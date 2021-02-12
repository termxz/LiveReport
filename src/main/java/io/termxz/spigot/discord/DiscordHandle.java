package io.termxz.spigot.discord;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.discord.commands.ProfileDiscord;
import io.termxz.spigot.discord.commands.ReportDiscord;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Map;

public class DiscordHandle {

    private JDA jda;

    private String reportsChannelID;
    private boolean initiated;

    public DiscordHandle(String token, String reportsChannelID, String commandsChannelID, String commandPrefix)   {
        if(!LiveReport.getPlugin().getConfig().getBoolean("DISCORD_ENABLED")) return;

        this.reportsChannelID = reportsChannelID;
        if(Bukkit.getServer().getPluginManager().getPlugin("JDA") != null) {
            try {
                jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES).
                        setStatus(OnlineStatus.ONLINE).
                        setActivity(Activity.watching("New Reports!")).
                        addEventListeners(new ProfileDiscord(commandPrefix, commandsChannelID),
                                new ReportDiscord(commandPrefix, commandsChannelID)).
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
}
