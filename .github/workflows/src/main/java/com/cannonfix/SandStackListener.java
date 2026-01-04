package com.cannonfix;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SandStackListener implements Listener {

    private final CannonFix plugin;
    private final Map<UUID, Long> recentFallingBlocks = new HashMap<>();
    private final Map<Location, Integer> stackCounts = new HashMap<>();

    public SandStackListener(CannonFix plugin) {
        this.plugin = plugin;
        
        // Clean up old entries every 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                recentFallingBlocks.entrySet().removeIf(entry -> now - entry.getValue() > 10000);
                stackCounts.clear();
            }
        }.runTaskTimer(plugin, 600L, 600L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFallingBlockSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;
        
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        Material material = fallingBlock.getBlockData().getMaterial();
        
        // Only handle sand/gravel types
        if (!isFallingBlockType(material)) return;
        
        // Mark as cannon-related falling block
        fallingBlock.setMetadata("cannonfix_sand", new FixedMetadataValue(plugin, System.currentTimeMillis()));
        
        // Track it
        recentFallingBlocks.put(fallingBlock.getUniqueId(), System.currentTimeMillis());
        
        // Ensure it doesn't drop item on death
        fallingBlock.setDropItem(true);
        
        // Set high tick limit to prevent early removal
        fallingBlock.setTicksLived(1);
        
        // Remove invulnerability ticks that might cause issues
        fallingBlock.setInvulnerable(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;
        
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        Material material = fallingBlock.getBlockData().getMaterial();
        
        if (!isFallingBlockType(material)) return;
        
        Location landLoc = event.getBlock().getLocation();
        Block targetBlock = landLoc.getBlock();
        
        // Check if the landing spot is valid
        if (targetBlock.getType() == Material.AIR || targetBlock.getType() == material) {
            // Valid landing - allow it
            return;
        }
        
        // If landing on a non-air block, find the next air block above
        if (plugin.getConfig().getBoolean("sand-stacking.auto-stack", true)) {
            Block above = targetBlock.getRelative(0, 1, 0);
            int maxStack = plugin.getConfig().getInt("sand-stacking.max-stack-height", 256);
            int count = 0;
            
            while (above.getType() != Material.AIR && count < maxStack) {
                above = above.getRelative(0, 1, 0);
                count++;
            }
            
            if (above.getType() == Material.AIR && count < maxStack) {
                // Cancel the original event and place block at new location
                event.setCancelled(true);
                
                // Place the block at the correct position
                final Block finalAbove = above;
                final Material finalMaterial = material;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (finalAbove.getType() == Material.AIR) {
                            finalAbove.setType(finalMaterial);
                        }
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallingBlockDropItem(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;
        
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        
        // Check if this is a protected falling block
        if (fallingBlock.hasMetadata("cannonfix_sand")) {
            Material material = fallingBlock.getBlockData().getMaterial();
            
            if (isFallingBlockType(material)) {
                // Try to place the block instead of dropping item
                if (plugin.getConfig().getBoolean("sand-stacking.prevent-drops", true)) {
                    Location loc = fallingBlock.getLocation();
                    Block block = loc.getBlock();
                    
                    // Find nearest air block
                    if (block.getType() != Material.AIR) {
                        for (int y = 0; y <= 5; y++) {
                            Block check = block.getRelative(0, y, 0);
                            if (check.getType() == Material.AIR) {
                                block = check;
                                break;
                            }
                        }
                    }
                    
                    if (block.getType() == Material.AIR) {
                        event.setCancelled(true);
                        block.setType(material);
                    }
                }
            }
        }
    }

    private boolean isFallingBlockType(Material material) {
        return material == Material.SAND ||
               material == Material.RED_SAND ||
               material == Material.GRAVEL ||
               material.name().endsWith("_CONCRETE_POWDER") ||
               material == Material.ANVIL ||
               material == Material.CHIPPED_ANVIL ||
               material == Material.DAMAGED_ANVIL ||
               material == Material.DRAGON_EGG ||
               material == Material.SUSPICIOUS_SAND ||
               material == Material.SUSPICIOUS_GRAVEL;
    }
}
