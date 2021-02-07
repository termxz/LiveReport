package io.termxz.spigot.gui;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.commands.ReportCommand;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.utils.inventory.InventoryFactory;
import io.termxz.spigot.utils.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SubmissionUI {

    private static final FileConfiguration guis = LiveReport.getPlugin().getConfigManager().getConfig("GUIs").getConfiguration();
    private static final String ITEMS_PATH = "SubmissionUI.MAIN.DEFAULT_ITEMS";
    private final long COOL_TIME = LiveReport.getPlugin().getConfig().getInt("REPORT_COOLDOWN_INT") * 1000L;

    public void create(Player viewer, OfflinePlayer offender) {

        InventoryFactory factory = new InventoryFactory(Message.GMessages.SUBMISSION_UI_TITLE.get(), 27, LiveReport.getPlugin()).
                createFromMap(guis.getConfigurationSection(ITEMS_PATH), e ->
                        new SubInventory().create(viewer, offender, Categories.fromItemName(e.getItemStack().getItemMeta().getDisplayName()))).
                addFiller(Material.GRAY_STAINED_GLASS_PANE);

        viewer.closeInventory();
        viewer.openInventory(factory.build());
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0F, 0.5F);
    }

    private class SubInventory {

        public void create(Player viewer, OfflinePlayer offender, Categories category) {

            String PATH = "SubmissionUI.CATEGORY_" + category.name() + ".";

            InventoryFactory factory = new InventoryFactory(category.getCategoryTitle(), guis.getInt(PATH + "SIZE"), LiveReport.getPlugin()).
                    createFromMap(guis.getConfigurationSection(PATH + "ITEMS"), e -> onItemAction(e, viewer, offender, category.getCategoryName())).
                    addBorder(Material.RED_STAINED_GLASS_PANE);

            viewer.closeInventory();
            viewer.openInventory(factory.build());
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0F, 0.5F);
        }

        private void onItemAction(InventoryFactory.ItemActionEvent e, Player reporter, OfflinePlayer offender, String reportType) {
            e.getPlayer().closeInventory();
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2.0F, 0.5F);

            final String reportReason = ChatColor.stripColor(e.getItemStack().getItemMeta().getDisplayName());

            Report report = new Report(offender, reporter, reportType, reportReason);
            LiveReport.getPlugin().getDB().submitReport(report);

            ReportCommand.coolPlayers.put(reporter.getUniqueId(), System.currentTimeMillis() + COOL_TIME);
        }
    }

    private enum Categories {

        HACK(Message.GMessages.CATEGORY_HACK_TITLE.get(), "Modified Client (Hacks)", guis.getString(ITEMS_PATH + ".HACK.display_name")),
        CHAT(Message.GMessages.CATEGORY_CHAT_TITLE.get(), "Chat Abuse", guis.getString(ITEMS_PATH + ".CHAT.display_name")),
        OTHER(Message.GMessages.CATEGORY_OTHER_TITLE.get(), "Other", guis.getString(ITEMS_PATH + ".OTHER.display_name"));

        private String categoryTitle, categoryName, itemName;

        Categories(String categoryTitle, String categoryName, String itemName) {
            this.categoryTitle = categoryTitle;
            this.categoryName = categoryName;
            this.itemName = itemName;
        }

        public static Categories fromItemName(String itemName) {
            for (Categories value : values()) {
                if(ChatColor.translateAlternateColorCodes('&', value.getItemName()).equals(itemName)) return value;
            }
            return null;
        }

        public String getCategoryName() { return categoryName; }

        public String getCategoryTitle() { return categoryTitle; }

        public String getItemName() { return itemName; }
    }
}
