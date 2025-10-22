package me.swissh.warped_dimension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WarpedMobSpawner extends BukkitRunnable {

    private final Random random = new Random();

    // Spawning settings
    private static final int SPAWN_RADIUS = 48; // 3 chunks radius
    private static final int MIN_DISTANCE_FROM_PLAYER = 24; // Minimum distance from player
    private static final int MAX_DISTANCE_FROM_PLAYER = 64; // Maximum distance from player
    private static final int MAX_ATTEMPTS_PER_CYCLE = 20; // Spawn attempts per cycle (increased)
    private static final double SPAWN_CHANCE = 0.6; // 60% chance per cycle per player (increased)
    private static final int MOB_CAP_PER_AREA = 12; // Max mobs per spawn area (increased)
    private static final int HERD_SIZE_MIN = 2; // Minimum herd size
    private static final int HERD_SIZE_MAX = 5; // Maximum herd size

    // Track processed areas to avoid duplicate spawning
    private final Set<String> processedAreas = new HashSet<>();

    public WarpedMobSpawner(Warped_dimension plugin) {
        // Constructor kept for compatibility but plugin field removed
    }

    @Override
    public void run() {
        World warpedWorld = Bukkit.getWorld("warped_dimension");
        if (warpedWorld == null) {
            return;
        }

        // Clear processed areas from last cycle
        processedAreas.clear();

        // Get all players in the warped dimension
        var players = warpedWorld.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // Try to spawn mobs near each player
        for (Player player : players) {
            String areaKey = getAreaKey(player.getLocation());

            // Skip if we already processed this area this cycle
            if (processedAreas.contains(areaKey)) {
                continue;
            }

            processedAreas.add(areaKey);

            // Check if we should attempt spawning
                if (random.nextDouble() < SPAWN_CHANCE) {
                attemptSpawnNearPlayer(player);
            }
        }
    }

    private void attemptSpawnNearPlayer(Player player) {
        Location playerLoc = player.getLocation();

        // Count existing passive mobs in the area
        int nearbyMobs = countPassiveMobsInArea(playerLoc);
        if (nearbyMobs >= MOB_CAP_PER_AREA) {
            return;
        }

        // Try multiple spawn attempts
        for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_CYCLE; attempt++) {
            Location spawnLoc = findValidSpawnLocation(playerLoc);
            if (spawnLoc != null) {
                spawnHerd(spawnLoc);
                break; // Only spawn one herd per cycle per player
            }
        }
    }

    private Location findValidSpawnLocation(Location center) {
        World world = center.getWorld();
        if (world == null) return null;

        // Random distance from player
        int distance = MIN_DISTANCE_FROM_PLAYER + random.nextInt(MAX_DISTANCE_FROM_PLAYER - MIN_DISTANCE_FROM_PLAYER);
        double angle = random.nextDouble() * 2 * Math.PI;
        
        int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
        int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);
        
        // Find the highest block at this location
        int y = world.getHighestBlockYAt(x, z);
        Location spawnLoc = new Location(world, x + 0.5, y + 1, z + 0.5);

        // Check if this is a valid spawn location
        if (isValidSpawnLocation(spawnLoc)) {
            return spawnLoc;
        }

        return null;
    }

    private boolean isValidSpawnLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return false;
        }

        // Check ground block
        Block groundBlock = loc.clone().subtract(0, 1, 0).getBlock();
        Material groundType = groundBlock.getType();
        
        // Must be on warped nylium or sculk (both biomes support this)
        if (groundType != Material.WARPED_NYLIUM && groundType != Material.SCULK) {
            return false;
        }

        // Check for air space above
        Block feetBlock = loc.getBlock();
        Block headBlock = loc.clone().add(0, 1, 0).getBlock();
        
        Material feetType = feetBlock.getType();
        Material headType = headBlock.getType();
        
        // Allow spawning on air or warped roots, with air above
        boolean validFeet = feetType == Material.AIR || feetType == Material.WARPED_ROOTS;
        boolean validHead = headType == Material.AIR;
        
        return validFeet && validHead;
    }

    private void spawnHerd(Location centerLoc) {
        World world = centerLoc.getWorld();
        if (world == null) return;

        // Determine herd size
        int herdSize = HERD_SIZE_MIN + random.nextInt(HERD_SIZE_MAX - HERD_SIZE_MIN + 1);
        
        // Choose primary mob type for the herd (70% cows, 30% chickens)
        EntityType primaryType = random.nextDouble() < 0.7 ? EntityType.COW : EntityType.CHICKEN;
        
        for (int i = 0; i < herdSize; i++) {
            // Add random offset for each mob in the herd
            double offsetX = (random.nextDouble() - 0.5) * 8; // Spread within 8 blocks
            double offsetZ = (random.nextDouble() - 0.5) * 8;
            
            Location spawnLoc = centerLoc.clone().add(offsetX, 0, offsetZ);
            
            // Check if this spawn location is valid
            if (isValidSpawnLocation(spawnLoc)) {
                // 80% chance to spawn the primary type, 20% chance for the other type
                EntityType type = (random.nextDouble() < 0.8) ? primaryType : 
                    (primaryType == EntityType.COW ? EntityType.CHICKEN : EntityType.COW);
                
                // Spawn the entity
                if (type == EntityType.COW) {
                    Cow cow = (Cow) world.spawnEntity(spawnLoc, EntityType.COW);
                    cow.setVariant(Cow.Variant.COLD);
                } else {
                    Chicken chicken = (Chicken) world.spawnEntity(spawnLoc, EntityType.CHICKEN);
                    chicken.setVariant(Chicken.Variant.COLD);
                }
            }
        }
    }

    private int countPassiveMobsInArea(Location center) {
        int count = 0;
        World world = center.getWorld();
        if (world == null) return 0;

        double radiusSquared = SPAWN_RADIUS * SPAWN_RADIUS;
        for (var entity : world.getEntities()) {
            if (entity.getType() == EntityType.COW || entity.getType() == EntityType.CHICKEN) {
                if (entity.getLocation().distanceSquared(center) <= radiusSquared) {
                    count++;
                }
            }
        }
        return count;
    }

    private String getAreaKey(Location loc) {
        // Group locations into 3x3 chunk areas to avoid duplicate spawning
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        int areaX = chunkX / 3;
        int areaZ = chunkZ / 3;
        return areaX + "," + areaZ;
    }
}
