package io.termxz.spigot.observer;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.data.report.ReportStatus;
import io.termxz.spigot.database.local.Config;
import org.bukkit.configuration.file.FileConfiguration;

public class StatisticsObserver implements Observer {

    Config statsConfig;

    public StatisticsObserver() {
        statsConfig = LiveReport.getPlugin().getConfigManager().getConfig("statistics");
    }

    @Override
    public void update(Report report) {
        FileConfiguration config = statsConfig.getConfiguration();

        if(report.getReportStatus().equals(ReportStatus.RESOLVED))
            config.set(ReportStatus.RESOLVED.name(), getGlobalResolved()+1);
        if(report.getReportStatus().equals(ReportStatus.DECLINED))
            config.set(ReportStatus.DECLINED.name(), getGlobalDeclined()+1);
        if(report.getReportStatus().equals(ReportStatus.PENDING_REVIEW))
            config.set("TOTAL_REPORTS_SUBMITTED", getGlobalReports()+1);
            config.set(ReportStatus.PENDING_REVIEW.name(), getGlobalPending()+1);

        statsConfig.saveConfig();
        statsConfig.reloadConfig();
    }

    public int getGlobalReports() {
        return statsConfig.getInt("TOTAL_REPORTS_SUBMITTED");
    }

    public int getGlobalResolved() {
        return statsConfig.getInt(ReportStatus.RESOLVED.name());
    }

    public int getGlobalDeclined() {
        return statsConfig.getInt(ReportStatus.DECLINED.name());
    }

    public int getGlobalPending() {
        return statsConfig.getInt(ReportStatus.PENDING_REVIEW.name());
    }
}
