package com.cannonfix;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;

public class ExplosionListener implements Listener {

    private final CannonFix plugin;

    public ExplosionListener(CannonFix plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        
        if (entity instanceof TNTPrimed) {
            // Ensure consistent explosion radius
            if (plugin.getConfig().getBoolean("tnt.consistent-radius", true)) {
                event.setRadius(4.0f); // Vanilla TNT radius
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Location explosionLoc = event.getLocation();
        
        if (entity instanceof TNTPrimed) {
            // Apply consistent velocity to nearby falling blocks
            if (plugin.getConfig().getBoolean("tnt.consistent-knockback", true)) {
                double radius = plugin.getConfig().getDouble("tnt.knockback-radius", 8.0);
                
                for (Entity nearby : explosionLoc.getWorld().getNearbyEntities(explosionLoc, radius, radius, radius)) {
                    if (nearby instanceof FallingBlock || nearby instanceof TNTPrimed) {
                        if (nearby.equals(entity)) continue; // Skip the exploding TNT
                        
                        // Calculate knockback vector
                        Vector direction = nearby.getLocation().toVector().subtract(explosionLoc.toVector());
                        double distance = direction.length();
                        
                        if (distance > 0 && distance <= radius) {
                            // Normalize and scale based on distance
                            direction.normalize();
                            double power = (radius - distance) / radius;
                            
                            // Apply velocity
                            Vector knockback = direction.multiply(power * plugin.getConfig().getDouble("tnt.knockback-power", 1.0));
                            nearby.setVelocity(nearby.getVelocity().add(knockback));
                        }
                    }
                }
            }
        }
    }
}
