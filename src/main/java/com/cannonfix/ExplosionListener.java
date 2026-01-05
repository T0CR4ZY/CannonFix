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
            if (plugin.getConfig().getBoolean("tnt.consistent-radius", true)) {
                event.setRadius(4.0f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Location explosionLoc = event.getLocation();
        
        if (entity instanceof TNTPrimed) {
            if (plugin.getConfig().getBoolean("tnt.consistent-knockback", true)) {
                double radius = plugin.getConfig().getDouble("tnt.knockback-radius", 8.0);
                
                for (Entity nearby : explosionLoc.getWorld().getNearbyEntities(explosionLoc, radius, radius, radius)) {
                    if (nearby instanceof FallingBlock || nearby instanceof TNTPrimed) {
                        if (nearby.equals(entity)) continue;
                        
                        Vector direction = nearby.getLocation().toVector().subtract(explosionLoc.toVector());
                        double distance = direction.length();
                        
                        if (distance > 0 && distance <= radius) {
                            direction.normalize();
                            double power = (radius - distance) / radius;
                            Vector knockback = direction.multiply(power * plugin.getConfig().getDouble("tnt.knockback-power", 1.0));
                            nearby.setVelocity(nearby.getVelocity().add(knockback));
                        }
                    }
                }
            }
        }
    }
}
