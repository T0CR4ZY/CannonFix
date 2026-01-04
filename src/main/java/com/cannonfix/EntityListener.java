package com.cannonfix;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityListener implements Listener {

    private final CannonFix plugin;

    public EntityListener(CannonFix plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        
        // Protect falling blocks from being removed
        if (entity instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) entity;
            
            // Mark it as protected
            fallingBlock.setMetadata("cannonfix_protected", new FixedMetadataValue(plugin, true));
            
            // Set properties to prevent issues
            fallingBlock.setDropItem(true);
            fallingBlock.setHurtEntities(false);
            
            // Increase despawn time / tick limit
            fallingBlock.setTicksLived(1);
        }
        
        // Protect TNT entities
        if (entity instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) entity;
            tnt.setMetadata("cannonfix_protected", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        // Prevent falling blocks from being killed by cramming
        if (entity instanceof FallingBlock) {
            if (event.getCause() == DamageCause.CRAMMING) {
                event.setCancelled(true);
            }
        }
        
        // Also protect TNT from cramming
        if (entity instanceof TNTPrimed) {
            if (event.getCause() == DamageCause.CRAMMING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        // This handles when falling block tries to become a block or drop
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            Material blockType = fallingBlock.getBlockData().getMaterial();
            
            // Check if it's a cannon-related material
            if (isSandType(blockType)) {
                // Let it land normally - don't interfere unless it's being deleted
                // The event being called means it's landing properly
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDropItem(EntityDropItemEvent event) {
        // This fires when a falling block drops as an item instead of placing
        Entity entity = event.getEntity();
        
        if (entity instanceof FallingBlock) {
            // Check if it should have placed instead
            FallingBlock fallingBlock = (FallingBlock) entity;
            
            // If it's dropping due to cramming/collision issues, we might want to handle this
            // For now, allow normal drops but log for debugging if needed
        }
    }

    private boolean isSandType(Material material) {
        return material == Material.SAND ||
               material == Material.RED_SAND ||
               material == Material.GRAVEL ||
               material == Material.WHITE_CONCRETE_POWDER ||
               material == Material.ORANGE_CONCRETE_POWDER ||
               material == Material.MAGENTA_CONCRETE_POWDER ||
               material == Material.LIGHT_BLUE_CONCRETE_POWDER ||
               material == Material.YELLOW_CONCRETE_POWDER ||
               material == Material.LIME_CONCRETE_POWDER ||
               material == Material.PINK_CONCRETE_POWDER ||
               material == Material.GRAY_CONCRETE_POWDER ||
               material == Material.LIGHT_GRAY_CONCRETE_POWDER ||
               material == Material.CYAN_CONCRETE_POWDER ||
               material == Material.PURPLE_CONCRETE_POWDER ||
               material == Material.BLUE_CONCRETE_POWDER ||
               material == Material.BROWN_CONCRETE_POWDER ||
               material == Material.GREEN_CONCRETE_POWDER ||
               material == Material.RED_CONCRETE_POWDER ||
               material == Material.BLACK_CONCRETE_POWDER ||
               material == Material.ANVIL ||
               material == Material.CHIPPED_ANVIL ||
               material == Material.DAMAGED_ANVIL;
    }
}
