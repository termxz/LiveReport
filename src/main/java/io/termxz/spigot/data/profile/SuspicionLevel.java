package io.termxz.spigot.data.profile;

import org.bukkit.ChatColor;

public enum SuspicionLevel {
    LOW_SUSPICION(ChatColor.GREEN), // 5
    MID_SUSPICION(ChatColor.YELLOW), // 10
    HIGH_SUSPICION(ChatColor.RED); // 15

    private ChatColor chatColor;
    SuspicionLevel(ChatColor chatColor) { this.chatColor = chatColor;}

    public ChatColor getChatColor() { return chatColor; }

    public static SuspicionLevel determineLevel(int level) {
        if(level >= 15)
            return HIGH_SUSPICION;
        else if (level >= 10)
            return MID_SUSPICION;
        else if (level >= 5)
            return LOW_SUSPICION;
        return LOW_SUSPICION;
    }

}
