package io.termxz.spigot.database.local;

import io.termxz.spigot.LiveReport;

import java.util.HashMap;

public class ConfigManager {

    private HashMap<String, Config> configs = new HashMap<>();

    public Config createConfig(String configName, String path, boolean fromJAR, boolean configUpdate) {

        Config config = new Config(configName, LiveReport.getPlugin(), fromJAR, configUpdate);
        config.setCustomPath(LiveReport.getPlugin().getDataFolder() + path);
        config.createConfig();
        configs.put(configName, config);

        return config;
    }

    public Config createConfig(String configName, String path) {
        return this.createConfig(configName, path, false ,false);
    }

    public Config getConfig(String configName) {
        return configs.get(configName);
    }

    public void reloadConfigs() {
        configs.values().forEach(Config::reloadConfig);
    }

}
