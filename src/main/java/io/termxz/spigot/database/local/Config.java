package io.termxz.spigot.database.local;

import com.google.common.base.Charsets;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Termxz
 *
 */

public class Config {

    private final JavaPlugin plugin;
    private final String configName;
    private String path;

    private final boolean fromJAR;
    private final boolean configUpdate;

    private FileConfiguration configuration;
    private File file;

    /**
     * Configuration Object used to easily create configs
     * @param name Name of the file
     * @param plugin JavaPlugin host
     * @param fromJAR From an embedded resource
     * @param configUpdate Supports configuration updates
     */

    public Config(String name, JavaPlugin plugin, boolean fromJAR, boolean configUpdate) {
        this.plugin = plugin;
        this.fromJAR = fromJAR;
        this.configUpdate = configUpdate;
        this.configName = name + ".yml";
        this.path = plugin.getDataFolder().getPath();
    }

    public void setCustomPath(String path) {
        this.path = path;
    }

    public void createConfig() {
        file = new File(path, configName);

        if (!plugin.getDataFolder().exists() || !file.getParentFile().exists()) {
            plugin.getDataFolder().mkdir();
            file.getParentFile().mkdir();
        }

        try {
            if (file.createNewFile()) {
                plugin.getLogger().log(Level.INFO, "Created {0} config file.", configName);
                if (fromJAR) { addDefaults(); }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create config file {0}.", configName);
            e.printStackTrace();
        }

        configuration = YamlConfiguration.loadConfiguration(file);
        updateConfig();
    }

    private void updateConfig() {
        if(configUpdate) {
            Reader reader = new InputStreamReader(plugin.getResource(configName), Charsets.UTF_8);
            FileConfiguration localConfig = YamlConfiguration.loadConfiguration(reader);

            Map<String, Object> configKeys = configuration.getValues(true);
            double configVersion = Double.parseDouble(configKeys.get("CONFIG_VERSION").toString());

            Map<String, Object> localKeys = localConfig.getValues(true);
            double localVersion = Double.parseDouble(localKeys.get("CONFIG_VERSION").toString());

            if(configVersion < localVersion) {
                plugin.getLogger().log(Level.INFO, "Attempting to update config file({0}): {1} to {2}.",new Object[]{configName, configVersion, localVersion});
                plugin.saveResource(configName, true);
                try {
                    Path path = Paths.get(plugin.getDataFolder() + File.separator + configName);
                    List<String> lines = Files.readAllLines(path, Charsets.UTF_8);

                    configKeys.entrySet().stream().filter(entry -> localKeys.containsKey(entry.getKey())).
                            forEach(entry -> {
                                String key = entry.getKey();
                                Object objectValue = entry.getValue();
                                if(key.equals("CONFIG_VERSION")) return;
                                if(objectValue instanceof MemorySection) return;

                                if(objectValue instanceof ArrayList) {
                                    ArrayList<Object> arrayList = (ArrayList) objectValue;

                                    boolean isSection = key.contains(".");
                                    String indexSpace = isSection ? StringUtils.repeat("  ", getSpaceAmount(key)) : "";
                                    if(isSection) {
                                        String[] array = key.split("\\.");
                                        key = array[array.length-1];
                                    }

                                    int startIndex = lines.indexOf(indexSpace + key + ":") + 1;
                                    String space = isSection ? StringUtils.repeat("  ", getSpaceAmount(key)+1) : "  ";

                                    for (Object o : arrayList) {
                                        lines.set(startIndex, space + "- " + safeValue(o));
                                        startIndex++;
                                    }
                                    return;
                                }

                                String space = StringUtils.repeat("  ", getSpaceAmount(key));
                                String index = space + stripKey(key) + ": " + safeValue(localKeys.get(key));
                                String value = space + stripKey(key) + ": " + safeValue(objectValue);

                                lines.set(lines.indexOf(index), value);
                            });

                    Files.write(path, lines, Charsets.UTF_8);
                    plugin.getLogger().log(Level.INFO, "Successfully updated config file: {0}", configName);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to update config file: {0}", configName);
                    e.printStackTrace();
                }
            }
        }
    }

    private int getSpaceAmount(String string) {
        return string.split("\\.").length - 1;
    }

    private String safeValue(Object object) {
        if(object instanceof String)
            return "\"" + object + "\"";
        return object.toString();
    }

    private String stripKey(String key) {
        if(key.contains(".")) {
            String[] array = key.split("\\.");
            return array[array.length-1];
        }
        return key;
    }

    private void addDefaults() {
        if (plugin.getResource(configName) != null) {
            plugin.saveResource(configName, true);
        }
    }

    public void saveConfig() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public String getString(String path) {
        return (configuration.contains(path) || configuration.getString(path) != null) ? configuration.getString(path) : "null";
    }

    public List<String> getStringList(String path) {
        return (configuration.contains(path) || configuration.get(path) != null) ? configuration.getStringList(path) : Collections.emptyList();
    }

    public int getInt(String path) {
        return (configuration.contains(path) || configuration.get(path) != null) ? configuration.getInt(path) : 0;
    }

}