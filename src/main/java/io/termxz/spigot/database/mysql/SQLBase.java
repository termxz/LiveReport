package io.termxz.spigot.database.mysql;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.profile.ReportProfile;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.database.IDataBase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.*;

public class SQLBase implements IDataBase {

    private Connection connection;

    private SQLBase(String server, String port, String username, String password, String name, JavaPlugin plugin) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + name + "?user=" + username + "&password=" + password);
            plugin.getLogger().info("Successfully connected to MySQL Database.");
            createTable(Tables.REPORTS);
            createTable(Tables.REPORT_PROFILES);
        }catch (Exception e) {
            plugin.getLogger().info("Failed to create initial connection to MySQL Server, Disabling LiveReport...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public SQLBase(Map<String, Object> map, JavaPlugin javaPlugin) {
        this(map.get("ip").toString(), map.get("port").toString(), map.get("username").toString(), map.get("password").toString(), map.get("name").toString(), javaPlugin);
    }

    private void createTable(Tables table) {
        try(PreparedStatement statement = connection.prepareStatement(table.getCreateCommand())) {
            if(!tableExist(table.getTableName()))
                statement.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getServer().getLogger().severe("[LiveReport] Failed to create Table: " + table.getTableName());
        }
    }

    private boolean tableExist(final String TABLE_NAME) {

        boolean tableExists = false;

        try (ResultSet rs = connection.getMetaData().getTables(null, null, TABLE_NAME, null)) {
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME");
                if (tableName != null && tableName.equals(TABLE_NAME)) {
                    tableExists = true;
                    break;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            Bukkit.getLogger().severe("[LiveReport] Failed to check for table existence: " + TABLE_NAME);
        }

        return tableExists;
    }

    @Override
    public Report getReport(final String reportID) {
        final Map<String, Object> map = new SQLRequest(connection, Tables.REPORTS.getTableName()).retrieve().selectAll("reportID", reportID);
        return new Report(map);
    }

    @Override
    public void getAsyncReport(final String reportID, RCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            Report report = getReport(reportID);
            Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> callback.call(report));
        });
    }

    @Override
    public void submitReport(final Report report) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(),  () -> {
            boolean reportExists = !(new SQLRequest(connection, Tables.REPORTS.getTableName()).retrieve().select("reportType").where("reportID", report.getReportID()).execute() == null);

            Map<String, Object> map = report.serialize(false);

            if(reportExists) {
                new SQLRequest(connection, Tables.REPORTS.getTableName()).submit().update(map, "reportID", report.getReportID()).execute();
            }else{
                new SQLRequest(connection, Tables.REPORTS.getTableName()).submit().insert(Tables.REPORTS.getTableFormat(), new ArrayList<>(map.values())).execute();
            }
            subjectNotify(report);
        });
    }

    @Override
    public void deleteReport(final Report report, boolean deleteAll) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            if(!deleteAll) {
                ReportProfile rp = getReportProfile(report.getOffenderUUID());
                rp.getActiveReports().remove(report.getReportID());
                rp.getArchivedReports().remove(report.getReportID());
                submitProfile(rp);
            }
            new SQLRequest(connection, Tables.REPORTS.getTableName()).plainSubmit("DELETE FROM " + Tables.REPORTS.getTableName() + " WHERE reportID='" + report.getReportID() + "'");
        });
    }

    @Override
    public ReportProfile getReportProfile(final UUID uuid) {
        final Map<String, Object> map = new SQLRequest(connection, Tables.REPORT_PROFILES.getTableName()).retrieve().selectAll("playerUUID", uuid.toString());
        return new ReportProfile(map);
    }

    @Override
    public void getAsyncProfile(final UUID uuid, PCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            ReportProfile rp = getReportProfile(uuid);
            Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> callback.call(rp));
        });
    }

    @Override
    public void submitProfile(final ReportProfile rp) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            final boolean profileExists = profileExists(rp.getPlayerUUID());

            Map<String, Object> map = rp.serialize(false);

            if(profileExists)
                new SQLRequest(connection, Tables.REPORT_PROFILES.getTableName()).submit().update(map, "playerUUID", rp.getPlayerUUID().toString()).execute();
            else
                new SQLRequest(connection, Tables.REPORT_PROFILES.getTableName()).submit().insert(Tables.REPORT_PROFILES.getTableFormat(), new ArrayList<>(map.values())).execute();
        });
    }

    @Override
    public void deleteProfile(final ReportProfile rp) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            new SQLRequest(connection, Tables.REPORTS.getTableName()).plainSubmit("DELETE FROM " + Tables.REPORT_PROFILES.getTableName() + " WHERE playerUUID='" + rp.getPlayerUUID() + "'");
        });
        loopReports(reportList -> reportList.forEach(report -> {
            if(report.getOffenderUUID().equals(rp.getPlayerUUID()) || report.getReporterUUID().equals(rp.getPlayerUUID()))
                deleteReport(report, true);
        }));
    }

    @Override
    public boolean profileExists(UUID uuid) {
        return !(new SQLRequest(connection, Tables.REPORT_PROFILES.getTableName()).retrieve().select("playerName").where("playerUUID", uuid.toString()).execute() == null);
    }

    @Override
    public boolean reportExists(String reportID) {
        return !(new SQLRequest(connection, Tables.REPORTS.getTableName()).retrieve().select("reportType").where("reportID", reportID).execute() == null);
    }

    @Override
    public void loopReports(RLCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            List<Report> reportList = new ArrayList<>();
            List<Map<String, Object>> dataList = new SQLRequest(connection, Tables.REPORTS.getTableName()).retrieve().selectMulti();
            dataList.forEach(map -> reportList.add(new Report(map)));
            Collections.sort(reportList);
            Collections.reverse(reportList);
            callback.call(reportList);
        });
    }

    @Override
    public List<ReportProfile> loopProfiles() {
        List<ReportProfile> profileList = new ArrayList<>();
        List<Map<String, Object>> dataList = new SQLRequest(connection, Tables.REPORT_PROFILES.getTableName()).retrieve().selectMulti();
        dataList.forEach(map -> profileList.add(new ReportProfile(map)));
        return profileList;
    }
}
