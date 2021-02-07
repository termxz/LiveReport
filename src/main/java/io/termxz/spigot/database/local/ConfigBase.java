package io.termxz.spigot.database.local;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.profile.ReportProfile;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.database.IDataBase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ConfigBase implements IDataBase {

    private static final String REPORTS_PATH = "/Reports/";
    private static final String PROFILES_PATH = "/Profiles/";

    @Override
    public Report getReport(String reportID) {
        Config config = LiveReport.getPlugin().getConfigManager().createConfig(reportID, REPORTS_PATH);
        return new Report(config.getConfiguration().getValues(true));
    }

    @Override
    public void getAsyncReport(String reportID, RCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            Report report = getReport(reportID);
            Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> callback.call(report));
        });
    }

    @Override
    public void submitReport(final Report report) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            Config config = LiveReport.getPlugin().getConfigManager().createConfig(report.getReportID(), REPORTS_PATH);

            config.getConfiguration().options().header("# You should never delete these type of files manually! Please use the in-game report tracker to delete them!");
            report.serialize(true).forEach((s, object) -> config.getConfiguration().set(s, object));

            config.saveConfig();
            config.reloadConfig();
            subjectNotify(report);
        });
    }

    @Override
    public void deleteReport(Report report, boolean deleteAll) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            if(!deleteAll) {
                ReportProfile rp = getReportProfile(report.getOffenderUUID());
                rp.getActiveReports().remove(report.getReportID());
                rp.getArchivedReports().remove(report.getReportID());
                submitProfile(rp);
            }
            new File(LiveReport.getPlugin().getDataFolder() + REPORTS_PATH, report.getReportID() + ".yml").delete();
        });
    }

    @Override
    public ReportProfile getReportProfile(UUID uuid) {
        Config config = LiveReport.getPlugin().getConfigManager().createConfig(uuid.toString(), PROFILES_PATH);
        return new ReportProfile(config.getConfiguration().getConfigurationSection("Profile").getValues(true));
    }

    @Override
    public void getAsyncProfile(UUID uuid, PCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            ReportProfile rp = getReportProfile(uuid);
            Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> callback.call(rp));
        });
    }

    @Override
    public void submitProfile(final ReportProfile submittedProfile) {

        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            Config config = LiveReport.getPlugin().getConfigManager().createConfig(submittedProfile.getPlayerUUID().toString(), PROFILES_PATH);

            config.getConfiguration().options().header("# You should never attempt to change any of these values unless you know what you're doing! (You have been warned)");
            submittedProfile.serialize(true).forEach((s, o) -> config.getConfiguration().set(s, o));
            config.saveConfig();
            config.reloadConfig();
        });
    }

    @Override
    public void deleteProfile(ReportProfile rp) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            new File(LiveReport.getPlugin().getDataFolder() + PROFILES_PATH, rp.getPlayerUUID() + ".yml").delete();
        });
        loopReports(reportList -> reportList.forEach(report -> {
           if(report.getOffenderUUID().equals(rp.getPlayerUUID()) || report.getReporterUUID().equals(rp.getPlayerUUID()))
               deleteReport(report, true);
        }));
    }

    @Override
    public boolean profileExists(UUID uuid) {
        return new File(LiveReport.getPlugin().getDataFolder() + PROFILES_PATH, uuid.toString() + ".yml").exists();
    }

    @Override
    public boolean reportExists(String reportID) {
        return new File(LiveReport.getPlugin().getDataFolder() + REPORTS_PATH, reportID + ".yml").exists();
    }

    @Override
    public void loopReports(RLCallback callback) {

        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            List<Report> list = new ArrayList<>();

            File[] files = new File(LiveReport.getPlugin().getDataFolder() + REPORTS_PATH).listFiles();
            if(files != null) {
                for (File file : files)
                    list.add(new Report(YamlConfiguration.loadConfiguration(file).getValues(true)));
            } Collections.sort(list); Collections.reverse(list);
            callback.call(list);
        });
    }

    @Override
    public List<ReportProfile> loopProfiles() {

        List<ReportProfile> list = new ArrayList<>();

        File[] files = new File(LiveReport.getPlugin().getDataFolder() + PROFILES_PATH).listFiles();
        if(files != null) {
            for (File file : files)
                list.add(new ReportProfile(YamlConfiguration.loadConfiguration(file).getConfigurationSection("Profile").getValues(true)));
        }

        return list;
    }

}