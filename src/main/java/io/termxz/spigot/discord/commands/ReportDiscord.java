package io.termxz.spigot.discord.commands;

import io.termxz.spigot.LiveReport;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class ReportDiscord extends ListenerAdapter {

    private String commandPrefix, commandsChannelID;

    public ReportDiscord(String commandPrefix, String commandsChannelID) {
        this.commandPrefix = commandPrefix;
        this.commandsChannelID = commandsChannelID;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) && event.getChannel() instanceof TextChannel) {
            TextChannel channel = event.getTextChannel();
            if (channel.getId().equals(commandsChannelID)) {
                String msg = event.getMessage().getContentDisplay();

                if(msg.startsWith(commandPrefix + "vr")) {
                    String[] args = msg.split(" ");

                    if(args.length == 1) {
                        channel.sendMessage(new MessageBuilder("> Invalid Arguments, $vr REPORT-ID").build()).queue();
                        return;
                    }

                    String reportID = args[1];

                    if(!LiveReport.getPlugin().getDB().reportExists(reportID)) {
                        channel.sendMessage(new MessageBuilder("> Failed to retrieve report data.").build()).queue();
                        return;
                    }

                    LiveReport.getPlugin().getDB().getAsyncReport(reportID, report -> {
                        EmbedBuilder embedBuilder = new EmbedBuilder().
                                setAuthor("LiveReport : ReportID(" + report.getReportID() + ")").
                                setColor(Color.MAGENTA).
                                setTimestamp(report.getReportDate().toInstant()).
                                setThumbnail("https://i.imgur.com/LRfESzg.png").
                                addField(new MessageEmbed.Field("Reason:", report.getReportReason(), true)).
                                addField(new MessageEmbed.Field("Type:", report.getReportType(), true)).
                                addField(new MessageEmbed.Field("Status:", report.getReportStatus().name(), true)).
                                addField(new MessageEmbed.Field("Reporter:", report.getReporterName(), true)).
                                addField(new MessageEmbed.Field("Offender:", report.getOffenderName(), true)).
                                addField(new MessageEmbed.Field("Server:", report.getReportLocation(), true));

                        channel.sendMessage(embedBuilder.build()).queue();
                    });
                }
            }
        }
    }
}
