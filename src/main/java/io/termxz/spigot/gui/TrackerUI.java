package io.termxz.spigot.gui;

import io.termxz.spigot.LiveReport;
import io.termxz.spigot.data.report.Report;
import io.termxz.spigot.data.report.ReportStatus;
import io.termxz.spigot.utils.ItemBuilder;
import io.termxz.spigot.utils.inventory.InventoryFactory;
import io.termxz.spigot.utils.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackerUI {

    private final FileConfiguration guis = LiveReport.getPlugin().getConfigManager().getConfig("GUIs").getConfiguration();
    private final String PATH = "TrackerUI.DEFAULT_ITEMS.";

    public void create(Player viewer, List<String> reports) {

        InventoryFactory factory = new InventoryFactory(Message.GMessages.TRACKER_UI_TITLE.get(), 56, LiveReport.getPlugin()).
                addBorder(Material.RED_STAINED_GLASS_PANE).
                addItem(9, new ItemBuilder("&eLoading...", Material.BOOKSHELF, 1).build());

        if(reports == null) {
            LiveReport.getPlugin().getDB().loopReports(reportList -> {
                factory.addItem(9, Material.RED_STAINED_GLASS_PANE);
                createFactories(factory, reportList, true);
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(LiveReport.getPlugin(), () -> {
                ArrayList<Report> reportList = new ArrayList<>();
                reports.forEach(s -> reportList.add(LiveReport.getPlugin().getDB().getReport(s)));
                factory.addItem(9, Material.RED_STAINED_GLASS_PANE);
                createFactories(factory, reportList, false);
            });
        }

        viewer.closeInventory();
        viewer.openInventory(factory.build());
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0F, 0.5F);
        LiveReport.getPlugin().getUiObserver().addObserver(viewer.getUniqueId());
    }

    private void createFactories(InventoryFactory startFactory, List<Report> reports, boolean tracker) {
        HashMap<Integer, List<Report>> dividedReports = divideReports(reports, tracker);
        dividedReports.forEach((integer, reportList) -> {
            if(integer == 0) {
                startFactory.setPage(new ObserverPage().create(startFactory, reportList), 0);
                return;
            }
            InventoryFactory factory = new InventoryFactory(Message.GMessages.TRACKER_UI_TITLE.get(), 56, LiveReport.getPlugin()).
                    addBorder(Material.RED_STAINED_GLASS_PANE);
            startFactory.addPage(new ObserverPage().create(factory, reportList));
        });

        ItemBuilder nextItem = new ItemBuilder(guis.getConfigurationSection(PATH + "NEXT_ITEM").getValues(true));
        ItemBuilder backItem = new ItemBuilder(guis.getConfigurationSection(PATH + "BACK_ITEM").getValues(true));
        ItemBuilder exitItem = new ItemBuilder(guis.getConfigurationSection(PATH + "EXIT_ITEM").getValues(true));

        ItemBuilder statsItem = new ItemBuilder(guis.getConfigurationSection(PATH + "STATS_ITEM").getValues(true)).
                addHolderMap(Message.Placeholders.getGlobalStatsHolders());

        for (Map.Entry<Integer, InventoryFactory> entry : startFactory.getPages().entrySet()) {
            InventoryFactory inventory = entry.getValue();
            int pageNumber = entry.getKey();

            inventory.addItem(guis.getInt(PATH + "STATS_ITEM.slot"), statsItem.build());

            inventory.addItem(guis.getInt(PATH + "EXIT_ITEM.slot"), exitItem.build(), this::exitAction);

            if(startFactory.getPages().containsKey(pageNumber+1))
                inventory.addItem(guis.getInt(PATH + "NEXT_ITEM.slot"), nextItem.build(), e -> moveAction(e, startFactory, true, pageNumber));
            if(startFactory.getPages().containsKey(pageNumber-1))
                inventory.addItem(guis.getInt(PATH + "BACK_ITEM.slot"), backItem.build(), e -> moveAction(e, startFactory, false, pageNumber));
        }
    }

    private void exitAction(InventoryFactory.ItemActionEvent e) {
        e.getPlayer().closeInventory();
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_CLOSE, 2.0F, 0.5F);
    }

    private void moveAction(InventoryFactory.ItemActionEvent e, InventoryFactory startFactory, boolean forward, int pageNumber) {
        int i = forward ? 1 : -1;
        e.getPlayer().openInventory(startFactory.getPages().get(pageNumber+i).build());
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0F, 0.5F);
    }

    private class ObserverPage {

        public InventoryFactory create(InventoryFactory factory, List<Report> reports) {

            int startSlot = 10;
            for (Report report : reports) {

                Map<String, Object> map = guis.getConfigurationSection(PATH + "REPORT_ITEM").getValues(true);
                Map<String, String> placeHolders = Message.Placeholders.getReportHolders(report);

                ItemBuilder reportItem = new ItemBuilder(map.get("display_name").toString()
                        .replaceAll("%reportID%", placeHolders.get("%reportID%")), map.get("item").toString(), 1, (ArrayList<String>) map.get("lore"));
                reportItem.addHolderMap(placeHolders);

                factory.addItem(startSlot, reportItem.build(), e -> new DataUI().create(e.getPlayer(), report, true));

                if (inColumn(startSlot + 1))
                    startSlot += 3;
                else startSlot++;
            }
            return factory;
        }

        private boolean inColumn(int spot) {
            int[] column0 = {0, 9, 18, 27, 36, 45};
            int[] column1 = {8, 17, 26, 35, 44, 53};
            for (int i : column0)
                if(i == spot) return true;
            for(int i : column1)
                if(i == spot) return true;
            return false;
        }
    }

    private HashMap<Integer, List<Report>> divideReports(List<Report> reports, boolean tracker) {
        HashMap<Integer, List<Report>> reportsPages = new HashMap<>();
        int pages = 0; int i = 0;
        for (Report report : reports) {
            if (!report.getReportStatus().equals(ReportStatus.PENDING_REVIEW) && tracker) continue;
            if (i == 28) {
                pages++;
                i = 0;
            }
            List<Report> list = new ArrayList<>();
            if (reportsPages.containsKey(pages)) list.addAll(reportsPages.get(pages));
            list.add(report);
            reportsPages.put(pages, list);
            i++;
        }
        return reportsPages;
    }
}