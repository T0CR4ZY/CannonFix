package com.cannonfix;

import org.bukkit.plugin.java.JavaPlugin;

public class CannonFix extends JavaPlugin {

    private static CannonFix instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        
        // Register commands
        getCommand("cannonfix").setExecutor(new CannonFixCommand(this));
        
        getLogger().info("CannonFix has been enabled!");
        getLogger().info("Falling block protection: ENABLED");
        getLogger().info("TNT physics fix: ENABLED");
        getLogger().info("Entity cramming bypass for falling blocks: ENABLED");
    }

    @Override
    public void onDisable() {
        getLogger().info("CannonFix has been disabled!");
    }

    public static CannonFix getInstance() {
        return instance;
    }
}
