package io.termxz.spigot.observer;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UIObserver implements Observer, Listener {

    private List<UUID> observers = new ArrayList<>();

    public UIObserver() {
        Bukkit.getPluginManager().registerEvents(this, LiveReport.getPlugin());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClose(InventoryCloseEvent e) {
        if(!observers.contains(e.getPlayer().getUniqueId())) return;

        String inventoryName = e.getView().getTitle();
        if(inventoryName.equals("Crafting")) return;

        checkInventory(inventoryName, e.getPlayer().getUniqueId());
    }

    public void addObserver(UUID uuid) {
        observers.add(uuid);
    }

    public List<UUID> getObservers() {
        return observers;
    }

    private void checkInventory(String inventoryName, UUID uuid) {
        if(inventoryName.equals(color(Message.GMessages.TRACKER_UI_TITLE.get()))) {
            observers.remove(uuid);
        } else if (inventoryName.equals(color(Message.GMessages.DATA_UI_TITLE.get()))) {
            observers.remove(uuid);
        } else if(inventoryName.equals(color(Message.GMessages.PROFILE_UI_TITLE.get()))) {
            observers.remove(uuid);
        }
    }

    private String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    @Override
    public void update(Report report) {

        for (UUID uuid : new ArrayList<>(observers)) {
            if(!Bukkit.getOfflinePlayer(uuid).isOnline()) observers.remove(uuid);

            Player p = Bukkit.getPlayer(uuid);
            String inventoryName = ChatColor.stripColor(p.getOpenInventory().getTitle());
            checkInventory(inventoryName, uuid);
            p.sendMessage(new MessageBuilder(Message.CMessages.UI_UPDATED_MESSAGE, true).get());
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_HURT, 2.0F, 0.5F);
            p.closeInventory();
        }
    }
}
