package io.termxz.spigot.commands;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.gui.SubmissionUI;
import io.termxz.spigot.utils.TimeUnit;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {

    public ReportCommand() {
        Bukkit.getPluginCommand("report").setExecutor(this);
    }

    public static final Map<UUID, Long> coolPlayers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!(commandSender instanceof Player)) { return true; }

        if (command.getLabel().equals("report")) {

            Player player = (Player) commandSender;

            if(!player.hasPermission(LiveReport.getPlugin().getPermissions().getReport())) {
                player.sendMessage(new MessageBuilder(Message.CMessages.NO_PERMISSION, false).get());
                return true;
            }

            if (args.length == 0) { player.sendMessage(new MessageBuilder(Message.CMessages.USAGE_REPORT_MESSAGE, true).get());return true; }
            if (args[0].equalsIgnoreCase(player.getName())) { player.sendMessage(new MessageBuilder(Message.CMessages.REPORT_ERROR_SELF, true).get()); return true; }
            if(coolPlayers.containsKey(player.getUniqueId()) && (System.currentTimeMillis() <= coolPlayers.get(player.getUniqueId()))) {
                final String time = TimeUnit.toString(Math.round((coolPlayers.get(player.getUniqueId()) - System.currentTimeMillis()) * 0.001));
                player.sendMessage(new MessageBuilder(Message.CMessages.REPORT_ERROR_COOLDOWN, true).addPlaceHolder("%time%", time).get());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 2.0F, 0.5F);
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if (!offlinePlayer.isOnline() ||
                        !LiveReport.getPlugin().getDB().profileExists(offlinePlayer.getUniqueId())) {
                    player.sendMessage(new MessageBuilder(Message.CMessages.REPORT_ERROR_INVALID, true).get());
                    return;
                }

                Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> new SubmissionUI().create(player, offlinePlayer));
            });
            return true;
        }
        return false;
    }
}
