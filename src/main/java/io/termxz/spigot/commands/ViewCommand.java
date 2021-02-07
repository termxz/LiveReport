package io.termxz.spigot.commands;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.gui.DataUI;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ViewCommand implements CommandExecutor {

    public ViewCommand() {
        Bukkit.getPluginCommand("vr").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) return true;

        if(command.getLabel().equals("vr")) {
            Player p = (Player) commandSender;

            boolean editingRights = p.hasPermission(LiveReport.getPlugin().getPermissions().getAdmin());

            if(!p.hasPermission(LiveReport.getPlugin().getPermissions().getUserView()) && !editingRights){
                p.sendMessage(new MessageBuilder(Message.CMessages.NO_PERMISSION, false).get());
                return true;
            }

            if(strings.length == 0) {
                p.sendMessage(new MessageBuilder(Message.CMessages.USAGE_VIEW_MESSAGE, true).get());
                return true;
            }

            final String reportID = strings[0].replace("\\", "");

            Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
                if(!LiveReport.getPlugin().getDB().reportExists(reportID)) {
                    p.sendMessage(new MessageBuilder(Message.CMessages.VIEW_REPORT_ERROR_INVALID, true).get());
                    return;
                }

                p.sendMessage(new MessageBuilder(Message.CMessages.GATHERING_REPORT_DATA, true).get());
                Report report = LiveReport.getPlugin().getDB().getReport(reportID);
                Bukkit.getScheduler().runTask(LiveReport.getPlugin(), () -> new DataUI().create(p, report, editingRights));
            });

            return true;
        }
        return false;
    }
}
