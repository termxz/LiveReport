package io.termxz.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class Permissions {

    private final Permission admin = new Permission("livereport.admin");
    private final Permission report = new Permission("livereport.report");
    private final Permission dev = new Permission("livereport.dev");
    private final Permission userView = new Permission("livereport.user_view");

    public Permissions() {
        Bukkit.getPluginManager().addPermission(admin);
        Bukkit.getPluginManager().addPermission(report);
        Bukkit.getPluginManager().addPermission(dev);
        Bukkit.getPluginManager().addPermission(userView);
    }

    public Permission getAdmin() {
        return admin;
    }

    public Permission getReport() {
        return report;
    }

    public Permission getDev() {
        return dev;
    }

    public Permission getUserView() {
        return userView;
    }
}
