package io.termxz.spigot.database;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.profile.ReportProfile;
import io.termxz.spigot.data.report.Report;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public interface IDataBase {

    /*

    All database operations must be done Asynchronously,
    while observers will have to comply with thread safety
    and run on the main thread.

    Callbacks will also be done through the main thread
    (@getAsyncReport, @getAsyncProfile)

     */

    Report getReport(final String reportID);
    void getAsyncReport(final String reportID, RCallback callback);
    void submitReport(final Report report);
    void deleteReport(final Report reportID, boolean deleteAll);

    ReportProfile getReportProfile(final UUID uuid);
    void getAsyncProfile(final UUID uuid, PCallback callback);
    void submitProfile(final ReportProfile rp);
    void deleteProfile(final ReportProfile rp);

    boolean profileExists(UUID uuid);
    boolean reportExists(String reportID);

    List<ReportProfile> loopProfiles();
    void loopReports(RLCallback callback);

    default void subjectNotify(Report report) {
        Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> LiveReport.getPlugin().getReportPublisher().notifyObservers(report));
    }

    interface RCallback {
        void call(Report report);
    }

    interface RLCallback {
        void call(List<Report> reportList);
    }

    interface PCallback {
        void call(ReportProfile rp);

    }

}
