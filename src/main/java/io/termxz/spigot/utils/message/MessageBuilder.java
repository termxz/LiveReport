package io.termxz.spigot.utils.message;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageBuilder {

    private String message, prefix;

    private List<String> messages;

    public MessageBuilder(String message, boolean prefix) {
        this.message = message;
        this.prefix = prefix ? color(Message.CMessages.PREFIX.get()) : "";
    }

    public MessageBuilder(Message.CMessages message, boolean prefix) {
        this(message.get(), prefix);
    }

    public MessageBuilder(List<String> messages) {
        this.messages = messages;
    }

    public MessageBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public void sendList(Player player) {
        messages.forEach(s -> player.sendMessage(color(s)));
    }

    public MessageBuilder addPlaceHolder(String placeHolder, String replace) {

        if(messages != null) {
            messages = messages.stream().map(s -> s.replaceAll(placeHolder, replace)).collect(Collectors.toList());
        } else {
            message = message.replaceAll(placeHolder, replace);
        }

        return this;
    }

    public MessageBuilder addPlaceHolder(Map<String, String> map) {

        if(messages != null) {
            messages = messages.stream().map(s -> s=StringUtils.replaceEach(s, map.keySet().toArray(new String[0]), map.values().toArray(new String[0]))).collect(Collectors.toList());
        } else {
            message = StringUtils.replaceEach(message, map.keySet().toArray(new String[0]), map.values().toArray(new String[0]));
        }
        return this;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String get() {
        return color(prefix + message);
    }

}
