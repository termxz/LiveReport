package io.termxz.spigot.gui;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.data.report.ReportStatus;
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

import java.util.UUID;

public class DataUI {

    private final FileConfiguration guis = LiveReport.getPlugin().getConfigManager().getConfig("GUIs").getConfiguration();

    public void create(Player viewer, Report report, boolean editingRights) {

        String MAIN_PATH = "ReportDataUI.";
        String ITEMS_PATH = MAIN_PATH + "DEFAULT_ITEMS.";

        InventoryFactory factory = new InventoryFactory(Message.GMessages.DATA_UI_TITLE.get(), guis.getInt(MAIN_PATH + "SIZE"), LiveReport.getPlugin()).
                addBorder(Material.MAGENTA_STAINED_GLASS_PANE).
                addFiller(Material.PURPLE_STAINED_GLASS_PANE);

        Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {

            ItemBuilder reportInfo = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "REPORT_INFO").getValues(true)).
                    addHolderMap(Message.Placeholders.getReportHolders(report));

            ItemBuilder offenderInfo = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "OFFENDER_INFO").getValues(true)).
                    asPlayerHead(report.getOffenderUUID()).
                    addDisplayHolder("%playerName%", report.getOffenderName()).
                    addHolderMap(Message.Placeholders.getProfileHolders(LiveReport.getPlugin().getDB().getReportProfile(report.getOffenderUUID())));

            ItemBuilder reporterInfo = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "REPORTER_INFO").getValues(true)).
                    asPlayerHead(report.getReporterUUID()).
                    addDisplayHolder("%playerName%", report.getReporterName()).
                    addHolderMap(Message.Placeholders.getProfileHolders(LiveReport.getPlugin().getDB().getReportProfile(report.getReporterUUID())));

            ItemBuilder exitItem = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "EXIT_ITEM").getValues(true));
            ItemBuilder deleteItem = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "DELETE_ITEM").getValues(true));

            ItemBuilder resolveItem = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "MARK_RESOLVED_ITEM").getValues(true));
            ItemBuilder declineItem = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "MARK_DECLINED_ITEM").getValues(true));

            ItemBuilder otherInfo = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "OTHER_INFO").getValues(true)).
                    addHolderMap(Message.Placeholders.getReportHolders(report)).
                    addExtraHolders(Bukkit.getOfflinePlayer(report.getOffenderUUID()));

            factory.addItem(guis.getInt(ITEMS_PATH + "REPORT_INFO.slot"), reportInfo.build());
            factory.addItem(guis.getInt(ITEMS_PATH + "OFFENDER_INFO.slot"), offenderInfo.build(), e -> profileClick(e, report.getOffenderUUID(), editingRights));
            factory.addItem(guis.getInt(ITEMS_PATH + "REPORTER_INFO.slot"), reporterInfo.build(), e -> profileClick(e, report.getReporterUUID(), editingRights));
            factory.addItem(guis.getInt(ITEMS_PATH + "OTHER_INFO.slot"), otherInfo.build());

            if(report.getReportStatus().equals(ReportStatus.PENDING_REVIEW) && editingRights) {
                factory.addItem(guis.getInt(ITEMS_PATH + "MARK_RESOLVED_ITEM.slot"), resolveItem.build(), e -> updateStatus(e.getPlayer(), report, ReportStatus.RESOLVED));
                factory.addItem(guis.getInt(ITEMS_PATH + "MARK_DECLINED_ITEM.slot"), declineItem.build(), e -> updateStatus(e.getPlayer(), report, ReportStatus.DECLINED));
            }

            if(Bukkit.getOfflinePlayer(report.getOffenderUUID()).isOnline()) {
                Player p = Bukkit.getPlayer(report.getOffenderUUID());

                ItemBuilder onlineData = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "ONLINE_INFO_OFFENDER").getValues(true)).
                        addHolderMap(Message.Placeholders.getPlayerHolders(p)).
                        addExtraHolders(p);

                factory.addItem(guis.getInt(ITEMS_PATH + "ONLINE_INFO_OFFENDER.slot"), onlineData.build(), e -> teleportViewer(e, p, editingRights));
            }

            if(Bukkit.getOfflinePlayer(report.getReporterUUID()).isOnline()) {
                Player p = Bukkit.getPlayer(report.getReporterUUID());

                ItemBuilder onlineData = new ItemBuilder(guis.getConfigurationSection(ITEMS_PATH + "ONLINE_INFO_REPORTER").getValues(true)).
                        addHolderMap(Message.Placeholders.getPlayerHolders(p)).
                        addExtraHolders(p);

                factory.addItem(guis.getInt(ITEMS_PATH + "ONLINE_INFO_REPORTER.slot"), onlineData.build(), e -> teleportViewer(e, p, editingRights));
            }

            if(editingRights)
                factory.addItem(guis.getInt(ITEMS_PATH + "DELETE_ITEM.slot"), deleteItem.build(), e -> deleteReport(e, report));
            factory.addItem(guis.getInt(ITEMS_PATH + "EXIT_ITEM.slot"), exitItem.build(), this::exitInventory);
        });

        viewer.closeInventory();
        viewer.openInventory(factory.build());
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0F, 0.5F);
        LiveReport.getPlugin().getUiObserver().addObserver(viewer.getUniqueId());
    }

    private void profileClick(InventoryFactory.ItemActionEvent e, UUID playerUUID, boolean editingRights) {
        if(!editingRights) return;
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        if(LiveReport.getPlugin().getDB().profileExists(playerUUID))
            new ProfileUI().create(e.getPlayer(), player);
    }

    private void updateStatus(Player p, Report report, ReportStatus reportStatus) {
        report.setReportStatus(reportStatus);
        LiveReport.getPlugin().getDB().submitReport(report);
        p.closeInventory();
    }

    private void exitInventory(InventoryFactory.ItemActionEvent e) {
        e.getPlayer().closeInventory();
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 2.0F, 0.5F);
    }

    private void teleportViewer(InventoryFactory.ItemActionEvent e, Player teleportTo, boolean editingRights) {
        if(!editingRights) return;
        e.getPlayer().teleport(teleportTo);
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2.0F, 0.5F);
    }

    private void deleteReport(InventoryFactory.ItemActionEvent e, Report report) {
        e.getPlayer().closeInventory();
        LiveReport.getPlugin().getUiObserver().update(null);
        LiveReport.getPlugin().getDB().deleteReport(report, false);
        e.getPlayer().sendMessage(new MessageBuilder(Message.CMessages.REPORT_DELETED_MESSAGE, true).addPlaceHolder("%reportID%", report.getReportID()).get());
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 2.0F, 0.5F);
    }

}
