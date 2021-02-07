package io.termxz.spigot.commands;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.observer.ResponseObserver;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlertsCommand implements CommandExecutor {

    public AlertsCommand() {
        Bukkit.getPluginCommand("alerts").setExecutor(this);

        // Wait for ResponseObserver to be registered
        Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            if(player.hasPermission(LiveReport.getPlugin().getPermissions().getAdmin())) ResponseObserver.alertsStaff.add(player.getUniqueId());
        }));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(!(commandSender instanceof Player)) return true;

        if(command.getLabel().equals("alerts") || command.getAliases().contains(s)) {

            Player player = (Player)commandSender;

            if(!player.hasPermission(LiveReport.getPlugin().getPermissions().getAdmin())) {
                player.sendMessage(new MessageBuilder(Message.CMessages.NO_PERMISSION, false).get());
                return true;
            }

            boolean alerted = ResponseObserver.alertsStaff.contains(player.getUniqueId());
            if(alerted) ResponseObserver.alertsStaff.remove(player.getUniqueId());
            else ResponseObserver.alertsStaff.add(player.getUniqueId());

            final String status = alerted ? (ChatColor.RED + "DISABLED") : (ChatColor.GREEN + "ENABLED");
            player.sendMessage(new MessageBuilder(Message.CMessages.ALERTS_STATUS_MESSAGE, true).addPlaceHolder("%status%", status).get());
            return true;
        }

        return false;
    }
}
