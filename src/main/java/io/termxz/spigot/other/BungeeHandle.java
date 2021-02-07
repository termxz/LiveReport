package io.termxz.spigot.other;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.observer.ResponseObserver;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeHandle implements PluginMessageListener {

    private final String CHANNEL_NAME_OUT = "livereport:bungee";
    private final String CHANNEL_NAME_IN = "livereport:spigot";

    public BungeeHandle() {
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(LiveReport.getPlugin(), CHANNEL_NAME_IN, this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(LiveReport.getPlugin(), CHANNEL_NAME_OUT);
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {

        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        if(!s.equals(CHANNEL_NAME_IN)) return;

        List<String> list = new ArrayList<>(Message.CMessages.BUNGEE_REPORT_NOTIFICATION.getMessages());
        list = list.stream().map(s1 -> s1=new MessageBuilder(s1, false).
                addPlaceHolder("%reportID%", input.readUTF()).
                addPlaceHolder("%reportOffender%", input.readUTF()).
                addPlaceHolder("%reportReporter%", input.readUTF()).
                addPlaceHolder("%reportReason%", input.readUTF()).
                addPlaceHolder("%reportType%", input.readUTF()).
                addPlaceHolder("%reportDate%", input.readUTF()).
                addPlaceHolder("%reportStatus%", input.readUTF()).
                addPlaceHolder("%reportLocation%", input.readUTF()).get()).collect(Collectors.toList());

        for (UUID uuid : ResponseObserver.alertsStaff) {
            for (String s1 : list) {
                Bukkit.getPlayer(uuid).sendMessage(s1);
            }
        }

    }

    public void notifyBungee(Report report) {

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(report.getReportID());
        output.writeUTF(report.getOffenderName());
        output.writeUTF(report.getReporterName());
        output.writeUTF(report.getReportReason());
        output.writeUTF(report.getReportType());
        output.writeUTF(report.getFancyDate());
        output.writeUTF(report.getReportStatus().name());
        output.writeUTF(report.getReportLocation());

        Bukkit.getServer().sendPluginMessage(LiveReport.getPlugin(), CHANNEL_NAME_OUT, output.toByteArray());
    }
}
