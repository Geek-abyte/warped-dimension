package me.swissh.warped_dimension;

import org.bukkit.*;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class WarpedMobSpawner extends BukkitRunnable {

    private final Warped_dimension plugin;
    private final Random random = new Random();

    public WarpedMobSpawner(Warped_dimension plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        World warpedWorld = Bukkit.getWorld("warped_dimension");
        if (warpedWorld == null) {
            return;
        }

        // Try to spawn mobs near players in the warped dimension
        warpedWorld.getPlayers().forEach(player -> {
            if (random.nextDouble() < 0.8) { // 80% chance per cycle (increased from 30%)
                spawnPassiveMobs(player.getLocation());
            }
        });
    }

    private void spawnPassiveMobs(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        // Random offset from player (20-50 blocks away)
        int offsetX = (random.nextInt(60) - 30);
        int offsetZ = (random.nextInt(60) - 30);

        Location spawnLoc = center.clone().add(offsetX, 0, offsetZ);

        // Find suitable spawn location (on solid ground)
        spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);

        // Check if location is valid
        if (!isValidSpawnLocation(spawnLoc)) {
            return;
        }

        // Spawn either cow or chicken
        EntityType type = random.nextBoolean() ? EntityType.COW : EntityType.CHICKEN;

        // Increased chance to spawn multiple
        int count = random.nextDouble() < 0.5 ? random.nextInt(3) + 1 : 1;

        for (int i = 0; i < count; i++) {
            Location individualLoc = spawnLoc.clone().add(
                random.nextDouble() * 2 - 1,
                0,
                random.nextDouble() * 2 - 1
            );

            // Spawn the entity with cold variants
            if (type == EntityType.COW) {
                Cow cow = (Cow) world.spawnEntity(individualLoc, EntityType.COW);
                cow.setVariant(Cow.Variant.COLD);
            } else {
                Chicken chicken = (Chicken) world.spawnEntity(individualLoc, EntityType.CHICKEN);
                chicken.setVariant(Chicken.Variant.COLD);
            }
        }
    }

    private boolean isValidSpawnLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        Material ground = loc.clone().subtract(0, 1, 0).getBlock().getType();
        Material atFeet = loc.getBlock().getType();
        Material atHead = loc.clone().add(0, 1, 0).getBlock().getType();

        // Must have warped nylium as ground and air space above
        return ground == Material.WARPED_NYLIUM &&
               (atFeet == Material.AIR || atFeet == Material.WARPED_ROOTS) &&
               atHead == Material.AIR;
    }
}
