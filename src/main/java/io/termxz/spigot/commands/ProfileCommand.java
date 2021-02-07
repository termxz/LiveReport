package io.termxz.spigot.commands;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.gui.ProfileUI;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {

    public ProfileCommand() {
        Bukkit.getPluginCommand("vp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(!(commandSender instanceof Player)) return true;

        if(command.getLabel().equals("vp") || command.getAliases().contains(s)) {

            Player p = ((Player) commandSender);

            if(!p.hasPermission(LiveReport.getPlugin().getPermissions().getAdmin())) {
                p.sendMessage(new MessageBuilder(Message.CMessages.NO_PERMISSION, false).get());
                return true;
            }

            if(strings.length == 0) {
                p.sendMessage(new MessageBuilder(Message.CMessages.USAGE_PROFILE_MESSAGE, true).get());
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(strings[0].replace("\\", ""));
                if(!LiveReport.getPlugin().getDB().profileExists(offlinePlayer.getUniqueId())) {
                    p.sendMessage(new MessageBuilder(Message.CMessages.VIEW_PROFILE_ERROR_INVALID, true).get());
                    return;
                }

                p.sendMessage(new MessageBuilder(Message.CMessages.GATHERING_PROFILE_DATA, true).get());
                Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> new ProfileUI().create(p, offlinePlayer));
            });
            return true;
        }

        return false;
    }
}
