package com.cannonfix;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
        
        // Register listeners - TNT physics only, no sand interference
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTPhysicsListener(this), this);
        
        // Register commands
        CannonFixCommand cmdExecutor = new CannonFixCommand(this);
        getCommand("cannonfix").setExecutor(cmdExecutor);
        getCommand("cannonfix").setTabCompleter(cmdExecutor);
        
        getLogger().info("========================================");
        getLogger().info("CannonFix v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Config auto-apply: ENABLED");
        getLogger().info("TNT physics optimization: ENABLED");
        getLogger().info("Gamerules: maxEntityCramming=0");
        getLogger().info("========================================");
        
        if (getConfig().getBoolean("auto-configure.enabled", true)) {
            getLogger().warning("RESTART your server for config changes to take full effect!");
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
            world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 0);
            getLogger().info("Set maxEntityCramming=0 in: " + world.getName());
        }
    }

    private void modifyServerConfigs() {
        File serverRoot = getServer().getWorldContainer();
        modifySpigotConfig(serverRoot);
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
                
                if (line.contains("tnt-entity-height-nerf:") && !line.contains("tnt-entity-height-nerf: 0")) {
                    lines.set(i, line.replaceAll("tnt-entity-height-nerf:.*", "tnt-entity-height-nerf: 0"));
                    modified = true;
                    getLogger().info("Set tnt-entity-height-nerf: 0");
                }
                
                if (line.contains("max-tnt-per-tick:") && !line.contains("max-tnt-per-tick: 5000")) {
                    lines.set(i, line.replaceAll("max-tnt-per-tick:.*", "max-tnt-per-tick: 5000"));
                    modified = true;
                    getLogger().info("Set max-tnt-per-tick: 5000");
                }
            }

            if (modified) {
                Files.write(spigotFile.toPath(), lines);
                getLogger().info("Modified spigot.yml");
            }
        } catch (IOException e) {
            getLogger().warning("Failed to modify spigot.yml: " + e.getMessage());
        }
    }

    private void modifyPaperGlobalConfig(File serverRoot) {
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
                
                if (line.contains("allow-permanent-block-break-exploits:") && line.contains("false")) {
                    lines.set(i, line.replaceAll("allow-permanent-block-break-exploits:.*", "allow-permanent-block-break-exploits: true"));
                    modified = true;
                    getLogger().info("Set allow-permanent-block-break-exploits: true");
                }
                
                if (line.contains("allow-headless-pistons:") && line.contains("false")) {
                    lines.set(i, line.replaceAll("allow-headless-pistons:.*", "allow-headless-pistons: true"));
                    modified = true;
                    getLogger().info("Set allow-headless-pistons: true");
                }
            }

            if (modified) {
                Files.write(paperGlobal.toPath(), lines);
                getLogger().info("Modified paper-global.yml");
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
                
                if (line.contains("falling-block-height-nerf:") && !line.contains("falling-block-height-nerf: 0")) {
                    lines.set(i, line.replaceAll("falling-block-height-nerf:.*", "falling-block-height-nerf: 0"));
                    modified = true;
                    getLogger().info("Set falling-block-height-nerf: 0");
                }
                
                if (line.contains("tnt-entity-height-nerf:") && !line.contains("tnt-entity-height-nerf: 0")) {
                    lines.set(i, line.replaceAll("tnt-entity-height-nerf:.*", "tnt-entity-height-nerf: 0"));
                    modified = true;
                    getLogger().info("Set tnt-entity-height-nerf: 0");
                }
                
                if (line.contains("redstone-implementation:") && !line.contains("VANILLA")) {
                    lines.set(i, line.replaceAll("redstone-implementation:.*", "redstone-implementation: VANILLA"));
                    modified = true;
                    getLogger().info("Set redstone-implementation: VANILLA");
                }
                
                if (line.contains("max-entity-collisions:") && !line.contains("max-entity-collisions: 0")) {
                    lines.set(i, line.replaceAll("max-entity-collisions:.*", "max-entity-collisions: 0"));
                    modified = true;
                    getLogger().info("Set max-entity-collisions: 0");
                }
            }

            if (modified) {
                Files.write(paperWorld.toPath(), lines);
                getLogger().info("Modified paper-world-defaults.yml");
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
