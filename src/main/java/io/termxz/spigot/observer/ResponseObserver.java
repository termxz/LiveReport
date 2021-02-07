package io.termxz.spigot.observer;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.api.events.ReportSubmittedEvent;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.data.report.ReportStatus;
import io.termxz.spigot.other.BungeeHandle;
import io.termxz.spigot.other.DiscordHandle;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;

import java.util.*;

public class ResponseObserver implements Observer, Listener {

    private DiscordHandle discordHandle;
    private BungeeHandle bungeeHandle;

    public static final List<UUID> alertsStaff = new ArrayList<>();

    private final Permission adminPermission;

    public ResponseObserver() {
        Bukkit.getPluginManager().registerEvents(this, LiveReport.getPlugin());
        adminPermission = LiveReport.getPlugin().getPermissions().getAdmin();

        bungeeHandle = new BungeeHandle();
        discordHandle = new DiscordHandle(LiveReport.getPlugin().getConfig().getConfigurationSection("Discord").getValues(true));

        startRemainder();
    }

    private void startRemainder() {
        int seconds = LiveReport.getPlugin().config().getInt("REPORT_TRACKER_REMAINDER_TIME");
        if(seconds == 0) return;
        Bukkit.getScheduler().runTaskTimer(LiveReport.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
                if(alertsStaff.contains(player.getUniqueId()) && player.hasPermission(adminPermission))
                    new MessageBuilder(Message.CMessages.REPORT_TRACKER_REMAINDER.getMessages()).addPlaceHolder(Message.Placeholders.getGlobalStatsHolders()).sendList(player);
        }), seconds * 20L, seconds * 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().hasPermission(adminPermission)) return;

        alertsStaff.add(e.getPlayer().getUniqueId());

        if(LiveReport.getPlugin().config().getConfiguration().getBoolean("REPORT_TRACKER_REMAINDER_ON_JOIN")) {
            new MessageBuilder(Message.CMessages.REPORT_TRACKER_REMAINDER.getMessages()).addPlaceHolder(Message.Placeholders.getGlobalStatsHolders()).sendList(e.getPlayer());
        }
    }

    @Override
    public void update(Report report) {

        Bukkit.getOnlinePlayers().forEach(player -> {

            if(report.getReporterUUID().equals(player.getUniqueId())) {

                if(!report.getReportStatus().equals(ReportStatus.PENDING_REVIEW)) {
                    player.sendMessage(new MessageBuilder(Message.CMessages.REPORT_STATUS_CHANGE, true).addPlaceHolder(Message.Placeholders.getReportHolders(report)).get());
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 2.0F, 0.5F);
                } else {
                    player.sendMessage(new MessageBuilder(Message.CMessages.SUCCESSFUL_REPORT_SUBMISSION, true).addPlaceHolder(Message.Placeholders.getReportHolders(report)).get());
                }

            }

            if(player.hasPermission(adminPermission) && alertsStaff.contains(player.getUniqueId()) && report.getReportStatus().equals(ReportStatus.PENDING_REVIEW)) {
                LiveReport.getPlugin().getDB().getAsyncProfile(report.getOffenderUUID(), rp -> {
                    List<String> list = new ArrayList<>(Message.CMessages.MC_REPORT_NOTIFICATION.getMessages());
                    Map<String, String> map = new HashMap<>(Message.Placeholders.getReportHolders(report));
                    map.putAll(Message.Placeholders.getProfileHolders(rp));

                    MessageBuilder messageBuilder = new MessageBuilder(list).addPlaceHolder(map);
                    messageBuilder.sendList(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 2.0F, 0.5F);
                });
            }
        });

        bungeeHandle.notifyBungee(report);
        discordHandle.notifyDiscord(report);
        Bukkit.getPluginManager().callEvent(new ReportSubmittedEvent(report));
    }
}
