package io.termxz.spigot.data.profile;

import io.termxz.spigot.utils.ISerialize;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportProfile implements ISerialize {

    private String playerName;
    private UUID playerUUID;

    private int amountOfReports;
    private int totalPlaytime;

    private String lastPlayed, firstPlayed;

    private LinkedList<String> activeReports, archivedReports;

    private SuspicionLevel suspicionLevel;

    public ReportProfile(Player player) {

        this.playerName = player.getName();
        this.playerUUID = player.getUniqueId();

        this.activeReports = new LinkedList<>();
        this.archivedReports = new LinkedList<>();

        this.totalPlaytime = 0;
        this.amountOfReports = 0;
        this.firstPlayed = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        this.lastPlayed = firstPlayed;

        this.suspicionLevel = SuspicionLevel.LOW_SUSPICION;
    }


    public ReportProfile(Map<String, Object> map) {
        this.playerName = map.get("playerName").toString();
        this.playerUUID = UUID.fromString(map.get("playerUUID").toString());
        this.activeReports = new LinkedList<>(cleanList(map.get("ActiveReports").toString()));
        this.archivedReports = new LinkedList<>(cleanList(map.get("ArchivedReports").toString()));
        this.totalPlaytime = (int)map.get("playTime");
        this.amountOfReports = (int)map.get("amountOfReports");
        this.firstPlayed = map.get("firstPlayed").toString();
        this.lastPlayed = map.get("lastPlayed").toString();
        this.suspicionLevel = SuspicionLevel.valueOf(map.get("suspicionLevel").toString());
    }

    @Override
    public Map<String, Object> serialize(boolean withKey) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        final String path = withKey ? "Profile." : "";

        map.put(path + "playerName", playerName);
        map.put(path + "playerUUID", playerUUID.toString());
        map.put(path + "amountOfReports", amountOfReports);
        map.put(path + "playTime", totalPlaytime);
        map.put(path + "firstPlayed", firstPlayed);
        map.put(path + "lastPlayed", lastPlayed);
        map.put(path + "suspicionLevel", suspicionLevel.name());

        map.put(path + "ActiveReports", activeReports);
        map.put(path + "ArchivedReports", archivedReports);

        return map;
    }

    private List<String> cleanList(String arrayString) {
        String string = arrayString.replaceAll("\\s+", "").replaceAll("\\[", "").replaceAll("]", "");
        String[] array = string.split(",");
        return string.equals("") ? Collections.emptyList(): Arrays.asList(array);
    }

    public String getPlayerName() { return playerName; }

    public UUID getPlayerUUID() { return playerUUID; }

    public LinkedList<String> getActiveReports() { return activeReports; }

    public LinkedList<String> getArchivedReports() { return archivedReports; }

    public long getTotalPlaytime() { return totalPlaytime; }

    public void addPlayTime(long totalPlaytime) { this.totalPlaytime += (totalPlaytime/1000); }

    public int getAmountOfReports() { return amountOfReports; }

    public void upAmountOfReports() { this.amountOfReports++; }

    public String getLastPlayed() { return lastPlayed; }

    public void setLastPlayed(String lastPlayed) { this.lastPlayed = lastPlayed; }

    public String getFirstPlayed() { return firstPlayed; }

    public SuspicionLevel getSuspicionLevel() { return suspicionLevel; }

    public void setSuspicionLevel(SuspicionLevel suspicionLevel) { this.suspicionLevel = suspicionLevel; }

}