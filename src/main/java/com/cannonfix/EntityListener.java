package com.cannonfix;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

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
            
            // Ensure proper behavior
            fallingBlock.setDropItem(true);
            fallingBlock.setHurtEntities(false);
            fallingBlock.setTicksLived(1);
            
            // Remove any velocity caps
            fallingBlock.setGravity(true);
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
            if (event.getCause() == DamageCause.CRAMMING || 
                event.getCause() == DamageCause.SUFFOCATION ||
                event.getCause() == DamageCause.CONTACT) {
                event.setCancelled(true);
            }
        }
        
        // Also protect TNT from cramming
        if (entity instanceof TNTPrimed) {
            if (event.getCause() == DamageCause.CRAMMING ||
                event.getCause() == DamageCause.SUFFOCATION) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityRemove(EntityRemoveEvent event) {
        Entity entity = event.getEntity();
        
        // Log removals for debugging if enabled
        if (plugin.getConfig().getBoolean("debug.log-entity-removal", false)) {
            if (entity instanceof FallingBlock || entity instanceof TNTPrimed) {
                plugin.getLogger().info("Entity removed: " + entity.getType() + " - Cause: " + event.getCause());
            }
        }
    }
}
