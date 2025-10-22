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
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        // Allow endermen to spawn naturally
        if (type == EntityType.ENDERMAN) {
            return;
        }

        // Allow warden with 0.02% chance (1 in 5000) for natural/spawner spawns
        if (type == EntityType.WARDEN) {
            if (reason == CreatureSpawnEvent.SpawnReason.NATURAL ||
                    reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                if (random.nextDouble() > 0.0002) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        // Handle passive mob spawning - convert to cold variants
        if (type == EntityType.COW || type == EntityType.CHICKEN) {
            // Allow all spawn reasons but convert to cold variants
            if (type == EntityType.COW && event.getEntity() instanceof Cow cow) {
                cow.setVariant(Cow.Variant.COLD);
            } else if (type == EntityType.CHICKEN && event.getEntity() instanceof Chicken chicken) {
                chicken.setVariant(Chicken.Variant.COLD);
            }
            return; // Allow the spawn
        }

        // Cancel all other mob spawns except natural passive mobs
        if (reason != CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
        }
    }

}
