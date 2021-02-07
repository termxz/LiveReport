package io.termxz.spigot.utils;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Update implements Listener {

    private String message = "";

    public Update() {
        initUpdater();
        Bukkit.getPluginManager().registerEvents(this, LiveReport.getPlugin());
    }

    private void initUpdater() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(LiveReport.getPlugin(), this::checkUpdate, 0L, 144000L);
    }

    private void checkUpdate() {
        try {
            final String currentVersion = LiveReport.getPlugin().getDescription().getVersion();

            URL checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=55896");
            URLConnection con = checkURL.openConnection();

            String newVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

            if(newVersion.contains("["))
                newVersion = newVersion.substring(0, newVersion.indexOf('['));

            if(!newVersion.equals(currentVersion)) {
                message = "A new update is available for LiveReport! New version: " + newVersion;
                LiveReport.getPlugin().getLogger().info(message);
            }
        } catch (Exception e) {
            LiveReport.getPlugin().getLogger().severe("Failed to check for new updates.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(p.hasPermission("livereport.dev"))
            p.sendMessage(new MessageBuilder(ChatColor.GREEN + message, true).get());
    }

}
