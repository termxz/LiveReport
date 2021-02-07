package io.termxz.spigot.other;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Map;
import java.util.UUID;

public class DiscordHandle {

    private JDA jda;

    private String commandPrefix, reportsChannelID, commandsChannelID;
    private boolean initiated;

    public DiscordHandle(String token, String reportsChannelID, String commandsChannelID, String commandPrefix)   {
        if(!LiveReport.getPlugin().getConfig().getBoolean("DISCORD_ENABLED")) return;

        this.commandPrefix = commandPrefix;
        this.reportsChannelID = reportsChannelID;
        this.commandsChannelID = commandsChannelID;
        if(Bukkit.getServer().getPluginManager().getPlugin("JDA") != null) {
            try {
                jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES).
                        setStatus(OnlineStatus.ONLINE).
                        setActivity(Activity.watching("New Reports!")).
                        addEventListeners(new Adapter()).
                        build();
                initiated = true;
                LiveReport.getPlugin().getLogger().info("Successfully hooked with JDA");
            } catch (LoginException e) {
                LiveReport.getPlugin().getLogger().info("Failed to establish Discord Connection using token: " + token);
            }
        } else {
            LiveReport.getPlugin().getLogger().info("Couldn't find JDA for Discord Implementation");
        }
    }

    public DiscordHandle(Map<String, Object> map) {
        this(map.get("token").toString(), map.get("reportsChannelID").toString(), map.get("commandsChannelID").toString(), map.get("command_prefix").toString());
    }

    public void notifyDiscord(Report report) {
        if(!initiated) return;

        EmbedBuilder embedBuilder = new EmbedBuilder().
                setAuthor("LiveReport : ReportID(" + report.getReportID() + ")").
                setColor(Color.RED).
                setTimestamp(report.getReportDate().toInstant()).
                setThumbnail("https://i.imgur.com/LRfESzg.png").
                addField(new MessageEmbed.Field("Reason:", report.getReportReason(), true)).
                addField(new MessageEmbed.Field("Type:", report.getReportType(), true)).
                addField(new MessageEmbed.Field("Status:", report.getReportStatus().name(), true)).
                addField(new MessageEmbed.Field("Reporter:", report.getReporterName(), true)).
                addField(new MessageEmbed.Field("Offender:", report.getOffenderName(), true)).
                addField(new MessageEmbed.Field("Server:", report.getReportLocation(), true));

        jda.getTextChannelById(reportsChannelID).sendMessage(embedBuilder.build()).queue();
    }

    class Adapter extends ListenerAdapter {

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
}
