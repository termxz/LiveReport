package io.termxz.spigot.commands;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    public ReloadCommand() {
        Bukkit.getPluginCommand("lr").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(command.getLabel().equals("lr")) {

            if(!commandSender.hasPermission(LiveReport.getPlugin().getPermissions().getDev())) {
                commandSender.sendMessage(new MessageBuilder(Message.CMessages.NO_PERMISSION, false).get());
                return true;
            }

            LiveReport.getPlugin().getConfigManager().reloadConfigs();
            commandSender.sendMessage(new MessageBuilder(Message.LR_RELOAD, true).get());
            return true;
        }
        return false;
    }
}
