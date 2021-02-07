package io.termxz.spigot.observer;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.profile.ReportProfile;
import io.termxz.spigot.data.profile.SuspicionLevel;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.data.report.ReportStatus;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ProfileObserver implements Observer, Listener {

    private HashMap<UUID, Long> playTime = new HashMap<>();

    public ProfileObserver() {
        Bukkit.getPluginManager().registerEvents(this, LiveReport.getPlugin());
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
                if(!LiveReport.getPlugin().getDB().profileExists(player.getUniqueId())) {
                    LiveReport.getPlugin().getDB().submitProfile(new ReportProfile(player));
                }
            });
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            if(!LiveReport.getPlugin().getDB().profileExists(e.getPlayer().getUniqueId())) {
                LiveReport.getPlugin().getDB().submitProfile(new ReportProfile(e.getPlayer()));
            }
        });
        playTime.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {

        Bukkit.getScheduler().runTaskLaterAsynchronously(LiveReport.getPlugin(), () -> {
            if(LiveReport.getPlugin().getDB().profileExists(e.getPlayer().getUniqueId())) {

                ReportProfile profile = LiveReport.getPlugin().getDB().getReportProfile(e.getPlayer().getUniqueId());
                if(playTime.containsKey(e.getPlayer().getUniqueId())) profile.addPlayTime(System.currentTimeMillis() - playTime.get(e.getPlayer().getUniqueId()));
                profile.setLastPlayed(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));

                LiveReport.getPlugin().getDB().submitProfile(profile);
                playTime.remove(e.getPlayer().getUniqueId());
            }
        }, 25L);
    }

    @Override
    public void update(Report report) {

        LiveReport.getPlugin().getDB().getAsyncProfile(report.getOffenderUUID(), rp -> {
            if(report.getReportStatus() == ReportStatus.PENDING_REVIEW) {
                rp.getActiveReports().add(report.getReportID());
            } else {
                rp.getArchivedReports().add(report.getReportID());
                rp.getActiveReports().remove(report.getReportID());
            }
            rp.upAmountOfReports();
            rp.setSuspicionLevel(SuspicionLevel.determineLevel(rp.getAmountOfReports()));

            LiveReport.getPlugin().getDB().submitProfile(rp);
        });
    }

}
