package io.termxz.spigot.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemBuilder {

    private String displayName;

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(String displayName, String materialType, int amount, List<String> lore) {
        this.displayName = displayName;

        try {
            itemStack = new ItemStack(Material.matchMaterial(materialType), amount);
            itemStack.setData(new ItemStack(Material.matchMaterial(materialType)).getData());
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("Invalid item type: " + materialType);
            itemStack = new ItemStack(Material.STONE, 1);
        }

        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        ArrayList<String> list = new ArrayList<>();
        lore.forEach(s -> list.add(ChatColor.translateAlternateColorCodes('&', s)));
        itemMeta.setLore(list);
    }

    public ItemBuilder(String displayName, Material material, int amount) {
        this(displayName, material.name(), amount, new ArrayList<>());
    }

    public ItemBuilder(Map<String, Object> map) {
        this(map.get("display_name").toString(), map.get("item").toString(), (int)map.get("amount"), (map.get("lore") instanceof MemorySection ? new ArrayList<>() : (ArrayList<String>) map.get("lore")));
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemBuilder asPlayerHead(UUID playerUUID) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
            skullMeta.setDisplayName(itemMeta.getDisplayName());
            skullMeta.setLore(itemMeta.getLore());
            this.itemStack.setItemMeta(skullMeta);
            itemMeta = skullMeta;
        }
        return this;
    }

    public ItemBuilder addDisplayHolder(String regex, String replace) {
        this.displayName = displayName.replaceAll(regex, replace);
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.displayName));
        return this;
    }

    public ItemBuilder addHolderMap(Map<String, String> map) {
        List<String> array = new ArrayList<>(itemMeta.getLore());
        array.forEach(s -> {
            String string = s;
            for (String value : map.keySet()) {
                string = string.replaceAll(value, map.get(value));
            }
            array.set(array.indexOf(s), string);
        });
        itemMeta.setLore(array);
        return this;
    }

    // PlaceHolderAPI

    public ItemBuilder addExtraHolders(OfflinePlayer player) {
        if(Bukkit.getPluginManager().getPlugin("PlaceHolderAPI") != null)
            itemMeta.setLore(PlaceholderAPI.setPlaceholders(player, itemMeta.getLore()));
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
