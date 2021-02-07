package io.termxz.spigot.gui;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.profile.ReportProfile;
import io.termxz.spigot.utils.ItemBuilder;
import io.termxz.spigot.utils.inventory.InventoryFactory;
import io.termxz.spigot.utils.message.Message;
import io.termxz.spigot.utils.message.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ProfileUI {

    private final FileConfiguration guis = LiveReport.getPlugin().getConfigManager().getConfig("GUIs").getConfiguration();
    private final String MAIN_PATH = "ReportProfileUI.";

    public void create(Player viewer, OfflinePlayer profilePlayer) {

        InventoryFactory factory = new InventoryFactory(Message.GMessages.PROFILE_UI_TITLE.get(),
                guis.getInt(MAIN_PATH + "SIZE"), LiveReport.getPlugin()).
                addBorder(Material.BLUE_STAINED_GLASS_PANE).
                addFiller(Material.LIGHT_BLUE_STAINED_GLASS_PANE).
                addItem(22, new ItemBuilder("&eLoading...", Material.BOOKSHELF, 1).build());

        LiveReport.getPlugin().getDB().getAsyncProfile(profilePlayer.getUniqueId(), rp -> {
            factory.addItem(22, Material.LIGHT_BLUE_STAINED_GLASS_PANE);

            String ITEMS_PATH = MAIN_PATH + "DEFAULT_ITEMS.";

            ItemBuilder activeReports = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "ACTIVE_REPORTS_INFO").getValues(true));
            ItemBuilder archivedReports = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "ARCHIVED_REPORTS_INFO").getValues(true));

            ItemBuilder playerInfo = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "PLAYER_INFO").getValues(true)).
                    addHolderMap(Message.Placeholders.getProfileHolders(rp)).
                    addDisplayHolder("%profileName%", rp.getPlayerName()).
                    asPlayerHead(rp.getPlayerUUID());

            ItemBuilder otherInfo = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "OTHER_INFO").getValues(true)).
                    addHolderMap(Message.Placeholders.getProfileHolders(rp)).
                    addExtraHolders(Bukkit.getOfflinePlayer(rp.getPlayerUUID()));

            ItemBuilder deleteItem = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "DELETE_ITEM").getValues(true));
            ItemBuilder exitItem = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "EXIT_ITEM").getValues(true));

            factory.addItem(guis.getInt(ITEMS_PATH + "ACTIVE_REPORTS_INFO.slot"), activeReports.build(), e -> new TrackerUI().create(e.getPlayer(), rp.getActiveReports()));
            factory.addItem(guis.getInt(ITEMS_PATH + "ARCHIVED_REPORTS_INFO.slot"), archivedReports.build(), e -> new TrackerUI().create(e.getPlayer(), rp.getArchivedReports()));
            factory.addItem(guis.getInt(ITEMS_PATH + "PLAYER_INFO.slot"), playerInfo.build());
            factory.addItem(guis.getInt(ITEMS_PATH + "OTHER_INFO.slot"), otherInfo.build());
            factory.addItem(guis.getInt(ITEMS_PATH + "EXIT_ITEM.slot"), exitItem.build(), this::exitAction);
            factory.addItem(guis.getInt(ITEMS_PATH + "DELETE_ITEM.slot"), deleteItem.build(), e -> deleteAction(e, rp));
        });

        viewer.closeInventory();
        viewer.openInventory(factory.build());
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 2.0F, 0.5F);
        LiveReport.getPlugin().getUiObserver().addObserver(viewer.getUniqueId());
    }

    private void deleteAction(InventoryFactory.ItemActionEvent e, ReportProfile rp) {
        LiveReport.getPlugin().getUiObserver().update(null);
        e.getPlayer().closeInventory();

        LiveReport.getPlugin().getDB().deleteProfile(rp);
        if(Bukkit.getOfflinePlayer(rp.getPlayerUUID()).isOnline()) {
            Player p = Bukkit.getPlayer(rp.getPlayerUUID());
            p.kickPlayer(Message.CMessages.ERROR_KICK_MESSAGE.get());
        }

        e.getPlayer().sendMessage(new MessageBuilder(Message.CMessages.PROFILE_DELETED_MESSAGE, true).addPlaceHolder(Message.Placeholders.getProfileHolders(rp)).get());
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 2.0F, 0.5F);
    }

    private void exitAction(InventoryFactory.ItemActionEvent e) {
        e.getPlayer().closeInventory();
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 2.0F, 0.5F);
    }

}
