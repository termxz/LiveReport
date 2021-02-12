package io.termxz.spigot.commands;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.gui.TrackerUI;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ObserverCommand implements CommandExecutor {

    public ObserverCommand() {
        Bukkit.getPluginCommand("rt").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(!(commandSender instanceof Player)) return true;

        Player p = (Player)commandSender;

        if(command.getLabel().equals("rt") || command.getAliases().contains(s)) {

            if(!p.hasPermission(LiveReport.getPlugin().getPermissions().getAdmin())) {
                p.sendMessage(new MessageBuilder(Message.CMessages.NO_PERMISSION, false).get());
                return true;
            }

            p.sendMessage(new MessageBuilder(Message.CMessages.GATHERING_REPORT_DATA, true).get());
            new TrackerUI().create(p, null);
            return true;
        }

        return false;
    }
}
