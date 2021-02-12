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
import java.util.UUID;

public class ProfileDiscord extends ListenerAdapter {

    private String commandPrefix, commandsChannelID;

    public ProfileDiscord(String commandPrefix, String commandsChannelID) {
        this.commandPrefix = commandPrefix;
        this.commandsChannelID = commandsChannelID;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) && event.getChannel() instanceof TextChannel) {
            TextChannel channel = event.getTextChannel();
            if (channel.getId().equals(commandsChannelID)) {
                String msg = event.getMessage().getContentDisplay();

                if (msg.startsWith(commandPrefix + "vp")) {
                    String[] args = msg.split(" ");

                    if (args.length == 1) {
                        channel.sendMessage(new MessageBuilder("> Invalid Arguments, $vp PLAYER_UUID").build()).queue();
                        return;
                    }

                    try {
                        UUID playerUUID = UUID.fromString(args[1]);
                        if (!LiveReport.getPlugin().getDB().profileExists(playerUUID)) {
                            channel.sendMessage(new MessageBuilder("> Failed to retrieve ReportProfile using UUID: ").append(playerUUID.toString()).build()).queue();
                        } else {
                            LiveReport.getPlugin().getDB().getAsyncProfile(playerUUID, rp -> {

                                EmbedBuilder embedBuilder = new EmbedBuilder().
                                        setAuthor("LiveReport : ReportProfile").
                                        setThumbnail("https://i.imgur.com/LRfESzg.png").
                                        setColor(Color.BLUE).
                                        addField(new MessageEmbed.Field("Name:", rp.getPlayerName(), true)).
                                        addField(new MessageEmbed.Field("UUID:", rp.getPlayerUUID().toString(), true)).
                                        addField(new MessageEmbed.Field("Suspicion Level:", rp.getSuspicionLevel().name(), true)).
                                        addField(new MessageEmbed.Field("Amount of Reports:", String.valueOf(rp.getAmountOfReports()), true)).
                                        addField(new MessageEmbed.Field("First Logged In:", rp.getFirstPlayed(), true)).
                                        addField(new MessageEmbed.Field("Last Logged In:", rp.getLastPlayed(), true));

                                channel.sendMessage(embedBuilder.build()).queue();
                            });
                        }
                    } catch (IllegalArgumentException e) {
                        channel.sendMessage(new MessageBuilder("> Invalid UUID: ").append(args[1]).build()).queue();
                    }
                }
            }
        }
    }
}