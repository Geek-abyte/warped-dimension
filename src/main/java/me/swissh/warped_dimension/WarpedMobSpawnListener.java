package me.swissh.warped_dimension;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

public class WarpedMobSpawnListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Only apply to warped dimension
        if (!event.getLocation().getWorld().getName().equals("warped_dimension")) {
            return;
        }

        EntityType type = event.getEntityType();

        // Allow endermen
        if (type == EntityType.ENDERMAN) {
            return;
        }

        // Allow warden with 0.02% chance (1 in 5000)
        if (type == EntityType.WARDEN) {
            if (random.nextDouble() > 0.0002) {
                event.setCancelled(true);
            }
            return;
        }

        // Allow cows and convert to cold variant
        if (type == EntityType.COW) {
            // Schedule conversion to cold variant after spawn
            if (event.getEntity() instanceof Cow cow) {
                cow.setVariant(Cow.Variant.COLD);
            }
            return;
        }

        // Allow chickens and convert to cold variant
        if (type == EntityType.CHICKEN) {
            if (event.getEntity() instanceof Chicken chicken) {
                chicken.setVariant(Chicken.Variant.COLD);
            }
            return;
        }

        // Cancel all other mob spawns
        event.setCancelled(true);
    }
}
