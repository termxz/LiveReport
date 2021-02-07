package io.termxz.spigot.utils.message;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.profile.ReportProfile;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.database.local.Config;
import io.termxz.spigot.observer.StatisticsObserver;
import io.termxz.spigot.utils.TimeUnit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message {

    private Message() {}

    /*

    Default Values

     */

    public static final String LR_RELOAD = "&aSuccessfully reloaded all configuration files!";


    /*

    Configuration Values

     */

    public enum CMessages {

        PREFIX,
        USAGE_REPORT_MESSAGE,
        USAGE_PROFILE_MESSAGE,
        USAGE_VIEW_MESSAGE,
        NO_PERMISSION,

        ERROR_KICK_MESSAGE,
        REPORT_ERROR_SELF,
        REPORT_ERROR_INVALID,
        REPORT_ERROR_COOLDOWN,

        SUCCESSFUL_REPORT_SUBMISSION,
        REPORT_STATUS_CHANGE,

        VIEW_PROFILE_ERROR_INVALID,
        VIEW_REPORT_ERROR_INVALID,

        ALERTS_STATUS_MESSAGE,

        REPORT_DELETED_MESSAGE,
        PROFILE_DELETED_MESSAGE,

        GATHERING_REPORT_DATA,
        GATHERING_PROFILE_DATA,

        UI_UPDATED_MESSAGE,

        REPORT_TRACKER_REMAINDER,

        MC_REPORT_NOTIFICATION,
        BUNGEE_REPORT_NOTIFICATION;

        private final Config config = LiveReport.getPlugin().config();

        public List<String> getMessages() {
            return config.getStringList(name());
        }

        public String get() {
            return config.getString(name());
        }

    }

    /*

    GUIs Values

     */

    public enum GMessages {

        PROFILE_UI_TITLE("ReportProfileUI.DEFAULT_TITLE"),
        TRACKER_UI_TITLE("TrackerUI.DEFAULT_TITLE"),
        SUBMISSION_UI_TITLE("SubmissionUI.DEFAULT_TITLE"),
        DATA_UI_TITLE("ReportDataUI.DEFAULT_TITLE"),

        CATEGORY_HACK_TITLE("SubmissionUI.CATEGORY_HACK.DEFAULT_TITLE"),
        CATEGORY_CHAT_TITLE("SubmissionUI.CATEGORY_CHAT.DEFAULT_TITLE"),
        CATEGORY_OTHER_TITLE("SubmissionUI.CATEGORY_OTHER.DEFAULT_TITLE");

        private final Config guis = LiveReport.getPlugin().getConfigManager().getConfig("GUIs");

        private String path;

        GMessages(String path) {
            this.path = path;
        }

        public String get() {
            return guis.getString(path);
        }
    }

    public static class Placeholders {

        public static Map<String, String> getProfileHolders(ReportProfile rp) {
            Map<String, String> map = new HashMap<>();
            map.put("%profileUUID%", rp.getPlayerUUID().toString());
            map.put("%profileName%", rp.getPlayerName());
            map.put("%profileAReports%", String.valueOf(rp.getAmountOfReports()));
            map.put("%profileLastLogin%", rp.getLastPlayed());
            map.put("%profileFirstLogin%", rp.getFirstPlayed());
            map.put("%profilePlayTime%", TimeUnit.toString(rp.getTotalPlaytime()));
            map.put("%profileLevel%", rp.getSuspicionLevel().name());
            return map;
        }

        public static Map<String, String> getReportHolders(Report report) {
            Map<String, String> map = new HashMap<>();
            map.put("%reportID%", report.getReportID());
            map.put("%reportOffender%", report.getOffenderName());
            map.put("%reportReporter%", report.getReporterName());
            map.put("%reportReason%", report.getReportReason());
            map.put("%reportType%", report.getReportType());
            map.put("%reportDate%", report.getFancyDate());
            map.put("%reportStatus%", report.getReportStatus().name());
            map.put("%reportLocation%", report.getReportLocation());
            return map;
        }

        public static Map<String, String> getPlayerHolders(Player p) {
            Map<String, String> map = new HashMap<>();
            map.put("%playerName%", p.getName());
            map.put("%playerUUID%", p.getUniqueId().toString());
            map.put("%playerLocale%", p.getLocale());
            map.put("%playerOP%", String.valueOf(p.isOp()));

            map.put("%playerGamemode%", p.getGameMode().name());
            map.put("%playerHealth%", String.valueOf(p.getHealth() / 2));
            map.put("%playerEXP%", String.valueOf(p.getExp()));
            return map;
        }

        public static Map<String, String> getGlobalStatsHolders() {
            Map<String, String> map = new HashMap<>();
            StatisticsObserver stats = LiveReport.getPlugin().getStatsObserver();
            map.put("%reportsSubmitted%", String.valueOf(stats.getGlobalReports()));
            map.put("%reportsResolved%", String.valueOf(stats.getGlobalResolved()));
            map.put("%reportsDeclined%", String.valueOf(stats.getGlobalDeclined()));
            map.put("%reportsPending%", String.valueOf(stats.getGlobalPending()));
            return map;
        }
    }
}
