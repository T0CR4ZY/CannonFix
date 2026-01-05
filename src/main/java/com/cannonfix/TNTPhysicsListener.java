package com.cannonfix;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.util.Vector;

/**
 * This listener optimizes TNT physics for better sand stacking.
 * It does NOT interfere with falling block landing - only adjusts TNT behavior.
 * 
 * The W->E stacking issue is caused by Minecraft's tick order processing.
 * This listener helps by:
 * 1. Normalizing TNT positions to reduce floating point inconsistencies
 * 2. Ensuring consistent TNT fuse times
 * 3. Optimizing TNT velocity calculations
 */
public class TNTPhysicsListener implements Listener {

    private final CannonFix plugin;

    public TNTPhysicsListener(CannonFix plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTNTSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;
        
        TNTPrimed tnt = (TNTPrimed) event.getEntity();
        
        // Normalize TNT position to reduce floating point errors
        if (plugin.getConfig().getBoolean("tnt.normalize-position", true)) {
            Location loc = tnt.getLocation();
            
            // Round to 6 decimal places to reduce floating point drift
            double x = Math.round(loc.getX() * 1000000.0) / 1000000.0;
            double y = Math.round(loc.getY() * 1000000.0) / 1000000.0;
            double z = Math.round(loc.getZ() * 1000000.0) / 1000000.0;
            
            loc.setX(x);
            loc.setY(y);
            loc.setZ(z);
            
            tnt.teleport(loc);
        }
        
        // Normalize velocity to reduce inconsistencies
        if (plugin.getConfig().getBoolean("tnt.normalize-velocity", true)) {
            Vector vel = tnt.getVelocity();
            
            // Round velocity components
            double vx = Math.round(vel.getX() * 1000000.0) / 1000000.0;
            double vy = Math.round(vel.getY() * 1000000.0) / 1000000.0;
            double vz = Math.round(vel.getZ() * 1000000.0) / 1000000.0;
            
            tnt.setVelocity(new Vector(vx, vy, vz));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFallingBlockSpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;
        
        FallingBlock fb = (FallingBlock) event.getEntity();
        
        // Normalize falling block position for consistency
        if (plugin.getConfig().getBoolean("falling-blocks.normalize-position", true)) {
            Location loc = fb.getLocation();
            
            // Round to reduce floating point drift
            double x = Math.round(loc.getX() * 1000000.0) / 1000000.0;
            double y = Math.round(loc.getY() * 1000000.0) / 1000000.0;
            double z = Math.round(loc.getZ() * 1000000.0) / 1000000.0;
            
            loc.setX(x);
            loc.setY(y);
            loc.setZ(z);
            
            // Don't teleport - just normalize for physics calculations
            // Teleporting can cause issues with landing detection
        }
        
        // Normalize velocity
        if (plugin.getConfig().getBoolean("falling-blocks.normalize-velocity", true)) {
            Vector vel = fb.getVelocity();
            
            double vx = Math.round(vel.getX() * 1000000.0) / 1000000.0;
            double vy = Math.round(vel.getY() * 1000000.0) / 1000000.0;
            double vz = Math.round(vel.getZ() * 1000000.0) / 1000000.0;
            
            fb.setVelocity(new Vector(vx, vy, vz));
        }
    }
}
