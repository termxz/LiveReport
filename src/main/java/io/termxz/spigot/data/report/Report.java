package io.termxz.spigot.data.report;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.utils.ISerialize;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Report implements ISerialize, Comparable<Report> {

    // Report Info
    private String reportType, reportReason, reportID;
    private Date reportDate; private final SimpleDateFormat dateFormat = new SimpleDateFormat("E, y-M-d h:m:ss a");
    private ReportStatus reportStatus;
    private String reportLocation;

    // Offender Info & Reporter Info
    private String offenderName, reporterName;
    private UUID offenderUUID, reporterUUID;

    public Report(OfflinePlayer offender, Player reporter, String reportType, String reportReason) {

        this.reportType = reportType;
        this.reportReason = reportReason;
        this.reportDate = new Date();
        this.reportID = UUID.randomUUID().toString().substring(0, 15);
        this.reportStatus = ReportStatus.PENDING_REVIEW;
        this.reportLocation = LiveReport.getPlugin().config().getString("SERVER_NAME");

        this.offenderName = offender.getName();
        this.reporterName = reporter.getName();

        this.offenderUUID = offender.getUniqueId();
        this.reporterUUID = reporter.getUniqueId();

    }

    public Report(Map<String, Object> map) {

        this.reportType = map.get("reportType").toString();
        this.reportReason = map.get("reportReason").toString();
        this.reportID = map.get("reportID").toString();
        this.reportStatus = ReportStatus.valueOf(map.get("reportStatus").toString());
        this.reportLocation = map.get("reportLocation").toString();
        this.offenderUUID = UUID.fromString(map.get("offenderUUID").toString());
        this.reporterUUID = UUID.fromString(map.get("reporterUUID").toString());
        this.reporterName = map.get("reporterName").toString();
        this.offenderName = map.get("offenderName").toString();

        try {
            this.reportDate = dateFormat.parse(map.get("reportDate").toString());
        } catch (Exception e) {
            this.reportDate = new Date();
        }

    }

    @Override
    public Map<String, Object> serialize(boolean withKey) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        map.put("reportType", reportType);
        map.put("reportReason", reportReason);
        map.put("reportDate", dateFormat.format(reportDate));
        map.put("reportID", reportID);
        map.put("reportStatus", reportStatus.name());
        map.put("reportLocation", reportLocation);

        map.put("offenderName", offenderName);
        map.put("reporterName", reporterName);

        map.put("offenderUUID", offenderUUID.toString());
        map.put("reporterUUID", reporterUUID.toString());

        return map;
    }

    public String getReportType() { return reportType; }

    public String getReportReason() { return reportReason; }

    public Date getReportDate() { return reportDate; }

    public String getFancyDate() { return dateFormat.format(reportDate); }

    public String getReportID() { return reportID; }

    public ReportStatus getReportStatus() { return reportStatus; }

    public String getOffenderName() { return offenderName; }

    public String getReporterName() { return reporterName; }

    public void setReportStatus(ReportStatus reportStatus) { this.reportStatus = reportStatus; }

    public String getReportLocation() { return reportLocation; }

    public UUID getOffenderUUID() { return offenderUUID; }

    public UUID getReporterUUID() { return reporterUUID; }

    @Override
    public int compareTo(Report o) { return getReportDate().compareTo(o.getReportDate()); }
}
