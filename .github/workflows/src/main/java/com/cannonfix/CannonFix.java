package com.cannonfix;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

public class CannonFix extends JavaPlugin {

    private static CannonFix instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Apply gamerules to all worlds
        applyGamerules();
        
        // Modify paper/spigot configs if enabled
        if (getConfig().getBoolean("auto-configure.enabled", true)) {
            modifyServerConfigs();
        }
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new SandStackListener(this), this);
        
        // Register commands
        CannonFixCommand cmdExecutor = new CannonFixCommand(this);
        getCommand("cannonfix").setExecutor(cmdExecutor);
        getCommand("cannonfix").setTabCompleter(cmdExecutor);
        
        getLogger().info("========================================");
        getLogger().info("CannonFix has been enabled!");
        getLogger().info("Falling block protection: ENABLED");
        getLogger().info("Sand stacking fix: ENABLED");
        getLogger().info("TNT physics fix: ENABLED");
        getLogger().info("Entity cramming bypass: ENABLED");
        getLogger().info("========================================");
        
        // Reminder to restart if configs were changed
        if (getConfig().getBoolean("auto-configure.enabled", true)) {
            getLogger().warning("Server configs have been optimized for cannoning.");
            getLogger().warning("Please RESTART your server for all changes to take effect!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("CannonFix has been disabled!");
    }

    public static CannonFix getInstance() {
        return instance;
    }

    private void applyGamerules() {
        for (World world : Bukkit.getWorlds()) {
            // Disable entity cramming
            if (getConfig().getBoolean("gamerules.disable-cramming", true)) {
                world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
                getLogger().info("Set maxEntityCramming to 0 in world: " + world.getName());
            }
        }
    }

    private void modifyServerConfigs() {
        File serverRoot = getServer().getWorldContainer();
        
        // Modify spigot.yml
        modifySpigotConfig(serverRoot);
        
        // Modify paper configs
        modifyPaperGlobalConfig(serverRoot);
        modifyPaperWorldConfig(serverRoot);
    }

    private void modifySpigotConfig(File serverRoot) {
        File spigotFile = new File(serverRoot, "spigot.yml");
        if (!spigotFile.exists()) {
            getLogger().warning("spigot.yml not found, skipping...");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(spigotFile.toPath());
            boolean modified = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                
                // Fix TNT entity height nerf
                if (line.contains("tnt-entity-height-nerf:")) {
                    lines.set(i, line.replaceAll("tnt-entity-height-nerf:.*", "tnt-entity-height-nerf: 0"));
                    modified = true;
                    getLogger().info("Set tnt-entity-height-nerf: 0");
                }
                
                // Fix max-tnt-per-tick
                if (line.contains("max-tnt-per-tick:")) {
                    lines.set(i, line.replaceAll("max-tnt-per-tick:.*", "max-tnt-per-tick: 5000"));
                    modified = true;
                    getLogger().info("Set max-tnt-per-tick: 5000");
                }
            }

            if (modified) {
                Files.write(spigotFile.toPath(), lines);
                getLogger().info("Modified spigot.yml for cannoning!");
            }
        } catch (IOException e) {
            getLogger().warning("Failed to modify spigot.yml: " + e.getMessage());
        }
    }

    private void modifyPaperGlobalConfig(File serverRoot) {
        // Check both old and new paper config locations
        File paperGlobal = new File(serverRoot, "config/paper-global.yml");
        if (!paperGlobal.exists()) {
            paperGlobal = new File(serverRoot, "paper.yml");
        }
        if (!paperGlobal.exists()) {
            getLogger().warning("paper-global.yml not found, skipping...");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(paperGlobal.toPath());
            boolean modified = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                
                // Allow permanent block break exploits (needed for sand)
                if (line.contains("allow-permanent-block-break-exploits:")) {
                    lines.set(i, line.replaceAll("allow-permanent-block-break-exploits:.*", "allow-permanent-block-break-exploits: true"));
                    modified = true;
                    getLogger().info("Set allow-permanent-block-break-exploits: true");
                }
                
                // Allow headless pistons (needed for some cannon designs)
                if (line.contains("allow-headless-pistons:")) {
                    lines.set(i, line.replaceAll("allow-headless-pistons:.*", "allow-headless-pistons: true"));
                    modified = true;
                    getLogger().info("Set allow-headless-pistons: true");
                }
            }

            if (modified) {
                Files.write(paperGlobal.toPath(), lines);
                getLogger().info("Modified paper-global.yml for cannoning!");
            }
        } catch (IOException e) {
            getLogger().warning("Failed to modify paper-global.yml: " + e.getMessage());
        }
    }

    private void modifyPaperWorldConfig(File serverRoot) {
        File paperWorld = new File(serverRoot, "config/paper-world-defaults.yml");
        if (!paperWorld.exists()) {
            getLogger().warning("paper-world-defaults.yml not found, skipping...");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(paperWorld.toPath());
            boolean modified = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                
                // Fix falling block height nerf
                if (line.contains("falling-block-height-nerf:")) {
                    lines.set(i, line.replaceAll("falling-block-height-nerf:.*", "falling-block-height-nerf: 0"));
                    modified = true;
                    getLogger().info("Set falling-block-height-nerf: 0");
                }
                
                // Fix TNT entity height nerf
                if (line.contains("tnt-entity-height-nerf:")) {
                    lines.set(i, line.replaceAll("tnt-entity-height-nerf:.*", "tnt-entity-height-nerf: 0"));
                    modified = true;
                    getLogger().info("Set tnt-entity-height-nerf: 0");
                }
                
                // Set redstone implementation to vanilla
                if (line.contains("redstone-implementation:")) {
                    lines.set(i, line.replaceAll("redstone-implementation:.*", "redstone-implementation: VANILLA"));
                    modified = true;
                    getLogger().info("Set redstone-implementation: VANILLA");
                }
                
                // Fix max entity collisions
                if (line.contains("max-entity-collisions:")) {
                    lines.set(i, line.replaceAll("max-entity-collisions:.*", "max-entity-collisions: 0"));
                    modified = true;
                    getLogger().info("Set max-entity-collisions: 0");
                }
            }

            if (modified) {
                Files.write(paperWorld.toPath(), lines);
                getLogger().info("Modified paper-world-defaults.yml for cannoning!");
            }
        } catch (IOException e) {
            getLogger().warning("Failed to modify paper-world-defaults.yml: " + e.getMessage());
        }
    }
    
    public void reloadPlugin() {
        reloadConfig();
        applyGamerules();
    }
}
