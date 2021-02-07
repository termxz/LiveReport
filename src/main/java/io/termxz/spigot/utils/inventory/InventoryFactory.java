package io.termxz.spigot.utils.inventory;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InventoryFactory implements Listener {

    private Inventory inventory;

    private String displayName;

    private int inventorySize;

    private List<Integer> possibleSizes = Arrays.asList(9, 18, 27, 36, 45, 54);

    private HashMap<ItemStack, ItemAction> itemActions = new HashMap<>();
    private HashMap<Integer, InventoryFactory> pages = new HashMap<>();

    private JavaPlugin plugin;

    public InventoryFactory(String displayName, int inventorySize, JavaPlugin plugin) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        this.inventorySize = findClosest(inventorySize);
        this.plugin = plugin;

        this.inventory = Bukkit.createInventory(null, this.inventorySize, this.displayName);
        this.pages.put(0, this);
    }

    public InventoryFactory addItem(int spot, ItemStack itemStack) {
        inventory.setItem(spot, itemStack);
        return this;
    }

    public InventoryFactory addItem(int spot, ItemStack itemStack, ItemAction itemAction) {
        addItem(spot, itemStack);
        itemActions.put(itemStack, itemAction);
        return this;
    }

    public InventoryFactory addItem(ItemStack itemStack, ItemAction itemAction) {
        inventory.addItem(itemStack);
        itemActions.put(itemStack, itemAction);
        return this;
    }

    public InventoryFactory addItem(int spot, Material material) {
        return addItem(spot, new ItemStack(material));
    }

    public InventoryFactory addFiller(ItemStack fillerItem) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            if(inventory.getItem(i) == null) inventory.setItem(i, fillerItem);
        }
        return this;
    }

    public InventoryFactory addFiller(Material material) {
        return addFiller(new ItemStack(material));
    }

    public InventoryFactory addBorder(ItemStack itemStack) {

        int rows = inventorySize / 9;

        // (9)1 row = 0, 8
        // (18)2 rows = 1:[0, 8] 2:[9, 17]
        // (27)3 rows = 1:[0 -> 8] 2:[9, 17] 3: [18 -> 26]

        /*int[] topBars = {0 , 1 , 2,  3,  4,  5,  6,  7,  8,
                         18, 19, 20, 21, 22, 23, 24, 25, 26,
                         27, 28, 29, 30, 31, 32, 33, 34, 35,
                         36, 37, 38, 39, 40, 41, 42, 43, 44,
                         45, 46, 47, 48, 49, 50, 51, 52, 53};*/

        if(rows >= 3) {
            for (int i = 0; i < 9; i++) { inventory.setItem(i, itemStack); } // Top Bar
            for (int i = (inventorySize - 9); i < inventorySize; i++) { inventory.setItem(i, itemStack); } // Bottom Bar
            for (int i = 0; i <= (inventorySize - 1); i+=9) {
                inventory.setItem(i, itemStack); // Left Side Bar
                inventory.setItem((i + 8), itemStack); // Right Side bar
            } // Side Bars
        }
        return this;
    }

    public InventoryFactory addBorder(Material material) {
        return addBorder(new ItemStack(material));
    }

    public InventoryFactory createFromMap(ConfigurationSection section, ItemAction itemAction) {

        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
            for (String key : section.getKeys(false)) {
                List<String> lore = section.getStringList(key + ".lore");
                ItemBuilder itemBuilder = new ItemBuilder(section.getString(key + ".display_name"),
                        section.getString(key + ".item"),
                        section.getInt(key + ".amount"), lore);

                if(itemAction != null)
                    addItem(section.getInt(key + ".slot"), itemBuilder.build(), itemAction);
                else
                    addItem(section.getInt(key + ".slot"), itemBuilder.build());
            }
        });

        return this;
    }

    public HashMap<Integer, InventoryFactory> getPages() {
        return pages;
    }

    public InventoryFactory addPage(InventoryFactory factory) {
        pages.put((pages.size()-1)+1, factory);
        return this;
    }

    public InventoryFactory setPage(InventoryFactory factory, int page) {
        if(pages.containsKey(page))
            pages.put(page, factory);
        return this;
    }

    public interface ItemAction {
        void onItemAction(ItemActionEvent e);
    }

    public class ItemActionEvent {

        private Player player;
        public Player getPlayer() {
            return player;
        }

        private ItemStack itemStack;
        public ItemStack getItemStack() {
            return itemStack;
        }

        private InventoryFactory clickedFactory;
        public InventoryFactory getClickedFactory() {
            return clickedFactory;
        }

        private int clickedSlot;
        public int getClickedSlot() {
            return clickedSlot;
        }

        private ClickType clickType;
        public ClickType getClickType() {
            return clickType;
        }

        public ItemActionEvent(Player player, ItemStack itemStack, InventoryFactory clickedFactory, int clickedSlot, ClickType clickType) {
            this.player = player;
            this.itemStack = itemStack;
            this.clickedFactory = clickedFactory;
            this.clickedSlot = clickedSlot;
            this.clickType = clickType;
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if(!(e.getWhoClicked() instanceof Player)) return;
        if(e.getClickedInventory() == null) return;
        if(!e.getClickedInventory().equals(inventory)) { e.setCancelled(true); return; }
        if(e.getInventory() == e.getWhoClicked().getInventory()) return;
        if(e.getCurrentItem() == null || !itemActions.containsKey(e.getCurrentItem())) { e.setCancelled(true); return; }

        e.setCancelled(true);

        ItemActionEvent event = new ItemActionEvent(((Player) e.getWhoClicked()).getPlayer(), e.getCurrentItem(), this, e.getSlot(), e.getClick());
        itemActions.get(e.getCurrentItem()).onItemAction(event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if(event.getInventory().equals(inventory)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent e) {
        if(e.getInventory().equals(inventory)){
            HandlerList.unregisterAll(this);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getInventorySize() {
        return inventorySize;
    }

    private int findClosest(int target) {
        int min = Integer.MAX_VALUE;
        int closest = target;

        for (int v : possibleSizes) {
            final int diff = Math.abs(v - target);

            if (diff < min)
            {
                min = diff;
                closest = v;
            }
        }
        return closest;
    }

    public Inventory build() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return inventory;
    }

}
