package io.termxz.bungee;

import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class LiveBridge extends Plugin implements Listener {

    private static final String CHANNEL_NAME_IN = "livereport:bungee";
    private static final String CHANNEL_NAME_OUT = "livereport:spigot";

    @Override
    public void onEnable() {
        getProxy().registerChannel(CHANNEL_NAME_IN);
        getProxy().registerChannel(CHANNEL_NAME_OUT);
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel(CHANNEL_NAME_IN);
        getProxy().registerChannel(CHANNEL_NAME_OUT);
    }

    /*

    Data Received & Distributed:

    0 SERVER_NAME
    1 REPORTER
    2 OFFENDER
    3 REASON
    4 TYPE

     */

    @EventHandler
    public void onReceive(PluginMessageEvent e) {
        if(!e.getTag().equals(CHANNEL_NAME_IN)) return;
        getProxy().getServers().values().forEach(serverInfo -> serverInfo.sendData(CHANNEL_NAME_OUT, e.getData(), true));
    }

}
