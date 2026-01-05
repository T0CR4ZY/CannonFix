package com.cannonfix;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;

public class CannonFix extends JavaPlugin {

    private static CannonFix instance;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        // Only set gamerule - don't touch any config files
        if (getConfig().getBoolean("set-gamerules", true)) {
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
                getLogger().info("Set maxEntityCramming=0 in: " + world.getName());
            }
        }
        
        // Only TNT explosion listener
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        
        CannonFixCommand cmdExecutor = new CannonFixCommand(this);
        getCommand("cannonfix").setExecutor(cmdExecutor);
        getCommand("cannonfix").setTabCompleter(cmdExecutor);
        
        getLogger().info("CannonFix v" + getDescription().getVersion() + " enabled!");
        getLogger().info("TNT optimization: ENABLED");
    }

    @Override
    public void onDisable() {
        getLogger().info("CannonFix disabled!");
    }

    public static CannonFix getInstance() {
        return instance;
    }
    
    public void reloadPlugin() {
        reloadConfig();
    }
}
