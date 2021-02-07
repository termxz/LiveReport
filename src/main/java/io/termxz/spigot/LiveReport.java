package io.termxz.spigot;

import io.termxz.spigot.commands.*;
import io.termxz.spigot.database.IDataBase;
import io.termxz.spigot.database.local.Config;
import io.termxz.spigot.database.local.ConfigBase;
import io.termxz.spigot.database.local.ConfigManager;
import io.termxz.spigot.database.mysql.SQLBase;
import io.termxz.spigot.observer.*;
import io.termxz.spigot.utils.MetricsLite;
import io.termxz.spigot.utils.Permissions;
import io.termxz.spigot.utils.Update;
import org.bukkit.plugin.java.JavaPlugin;

public class LiveReport extends JavaPlugin {

    private static LiveReport instance;
    public static LiveReport getPlugin() { return instance; }

    private Permissions permissions;

    private ReportSubject reportSubject;
    private ProfileObserver profileObserver;
    private StatisticsObserver statsObserver;
    private UIObserver uiObserver;

    private ConfigManager configManager;
    private IDataBase database;

    @Override
    public void onEnable() {
        instance = this;
        new Update();

        configManager = new ConfigManager();
        configManager.createConfig("config", "", true, true);
        configManager.createConfig("GUIs", "", true, false);
        configManager.createConfig("statistics", "", true, false);

        permissions = new Permissions();
        registerCommands();

        reportSubject = new ReportSubject();
        profileObserver = new ProfileObserver();
        statsObserver = new StatisticsObserver();
        uiObserver = new UIObserver();
        registerObservers();

        initDatabase();
        new MetricsLite(this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void registerObservers() {
        reportSubject.register(profileObserver);
        reportSubject.register(new ResponseObserver());
        reportSubject.register(statsObserver);
        reportSubject.register(uiObserver);
    }

    public ReportSubject getReportPublisher() {
        return reportSubject;
    }

    public StatisticsObserver getStatsObserver() {
        return statsObserver;
    }

    public UIObserver getUiObserver() {
        return uiObserver;
    }

    public Config config() {
        return configManager.getConfig("config");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private void initDatabase() {
        if(getConfig().getBoolean("MySQL_ENABLED"))
            database = new SQLBase(config().getConfiguration().getConfigurationSection("MySQL").getValues(true), this);
        else database = new ConfigBase();
    }

    public IDataBase getDB() {
        return database;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    private void registerCommands() {
        new ReportCommand();
        new ProfileCommand();
        new AlertsCommand();
        new ReloadCommand();
        new ObserverCommand();
        new ViewCommand();
    }

}