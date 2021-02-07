package io.termxz.spigot.database.mysql;

public enum Tables {

    REPORTS("live_reports",
            "(reportType TEXT, reportReason TEXT, reportDate TEXT, reportID TEXT, reportStatus TEXT, reportLocation TEXT, offenderName TEXT, reporterName TEXT, offenderUUID TEXT, reporterUUID TEXT)",
            "reportType", "reportReason", "reportDate", "reportID", "reportStatus", "reportLocation", "offenderName", "reporterName", "offenderUUID", "reporterUUID"),
    REPORT_PROFILES("report_profiles",
            "(playerName TEXT, playerUUID TEXT, amountOfReports INTEGER, playTime INTEGER, firstPlayed TEXT, lastPlayed TEXT, suspicionLevel TEXT, ActiveReports TEXT, ArchivedReports TEXT)",
            "playerName", "playerUUID", "amountOfReports", "playTime", "firstPlayed", "lastPlayed", "suspicionLevel","ActiveReports", "ArchivedReports");

    private final String tableName, createCommand;
    private final String[] values;

    Tables(String tableName, String createCommand, String... values) {
        this.tableName = tableName;
        this.createCommand = "CREATE TABLE " + tableName + " " + createCommand;
        this.values = values;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCreateCommand() {
        return createCommand;
    }

    public String getTableFormat() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            sb.append(i != (values.length-1) ? value + "," : value);
        }
        return sb.toString();
    }

    public String[] getValues() {
        return values;
    }
}
