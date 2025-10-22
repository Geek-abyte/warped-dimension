package me.swissh.warped_dimension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpedPortalListener implements Listener {

    private final Warped_dimension plugin;
    // Store portal locations for better linkage (world name -> list of portal locations)
    private final Map<String, List<Location>> portalRegistry = new HashMap<>();

    public WarpedPortalListener(Warped_dimension plugin) {
        this.plugin = plugin;
        // Start chunk preloading task
        startChunkPreloading();
    }

    private void startChunkPreloading() {
        // Preload chunks around portal locations every 5 seconds
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<String, List<Location>> entry : portalRegistry.entrySet()) {
                String worldName = entry.getKey();
                World world = plugin.getServer().getWorld(worldName);
                if (world == null) continue;

                for (Location portal : entry.getValue()) {
                    int chunkX = portal.getBlockX() >> 4;
                    int chunkZ = portal.getBlockZ() >> 4;

                    // Ensure chunks around portals are loaded
                    for (int x = chunkX - 1; x <= chunkX + 1; x++) {
                        for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                            if (!world.isChunkLoaded(x, z)) {
                                world.loadChunk(x, z);
                            }
                        }
                    }
                }
            }
        }, 100L, 100L); // Every 5 seconds (100 ticks)
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player is using flint and steel or fire charge
        Material item = player.getInventory().getItemInMainHand().getType();
        if (item != Material.FLINT_AND_STEEL && item != Material.FIRE_CHARGE) {
            return;
        }

        // Check if the clicked block is reinforced deepslate
        if (block.getType() != Material.REINFORCED_DEEPSLATE) {
            return;
        }

        // Try to create portal
        if (tryCreatePortal(block, player)) {
            event.setCancelled(true);
            player.sendMessage("§5Warped Portal activated!");
        }
    }

    private boolean tryCreatePortal(Block block, Player player) {
        // Check all possible portal orientations
        // Use SOUTH and WEST to flip the portal plane 180 degrees along y-axis
        BlockFace[] faces = {BlockFace.SOUTH, BlockFace.WEST};

        for (BlockFace face : faces) {
            Location portalLocation = findPortalFrame(block.getLocation(), face);
            if (portalLocation != null) {
                createPortal(portalLocation, face);
                return true;
            }
        }

        return false;
    }

    private Location findPortalFrame(Location loc, BlockFace orientation) {
        World world = loc.getWorld();
        if (world == null) return null;

        // Try different positions within the frame
        for (int offsetX = -4; offsetX <= 4; offsetX++) {
            for (int offsetY = -4; offsetY <= 4; offsetY++) {
                Location testLoc = loc.clone();

                if (orientation == BlockFace.SOUTH) {
                    testLoc.add(offsetX, offsetY, 0);
                } else {
                    testLoc.add(0, offsetY, offsetX);
                }

                if (isValidPortalFrame(testLoc, orientation)) {
                    return testLoc;
                }
            }
        }

        return null;
    }

    private boolean isValidPortalFrame(Location corner, BlockFace orientation) {
        World world = corner.getWorld();
        if (world == null) return false;

        // Check portal sizes from 4x5 (minimum nether portal size) to 23x23
        for (int width = 4; width <= 23; width++) {
            for (int height = 5; height <= 23; height++) {
                if (checkPortalFrame(corner, width, height, orientation)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkPortalFrame(Location corner, int width, int height, BlockFace orientation) {
        World world = corner.getWorld();
        if (world == null) return false;

        // Check if all frame blocks are reinforced deepslate
        // Check bottom
        for (int i = 0; i < width; i++) {
            Location loc = corner.clone();
            if (orientation == BlockFace.SOUTH) {
                loc.add(i, 0, 0);
            } else {
                loc.add(0, 0, i);
            }
            if (loc.getBlock().getType() != Material.REINFORCED_DEEPSLATE) {
                return false;
            }
        }

        // Check top
        for (int i = 0; i < width; i++) {
            Location loc = corner.clone();
            if (orientation == BlockFace.SOUTH) {
                loc.add(i, height - 1, 0);
            } else {
                loc.add(0, height - 1, i);
            }
            if (loc.getBlock().getType() != Material.REINFORCED_DEEPSLATE) {
                return false;
            }
        }

        // Check left side
        for (int i = 1; i < height - 1; i++) {
            Location loc = corner.clone().add(0, i, 0);
            if (loc.getBlock().getType() != Material.REINFORCED_DEEPSLATE) {
                return false;
            }
        }

        // Check right side
        for (int i = 1; i < height - 1; i++) {
            Location loc = corner.clone();
            if (orientation == BlockFace.SOUTH) {
                loc.add(width - 1, i, 0);
            } else {
                loc.add(0, i, width - 1);
            }
            if (loc.getBlock().getType() != Material.REINFORCED_DEEPSLATE) {
                return false;
            }
        }

        // Check if interior is air or can be filled
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                Location loc = corner.clone();
                if (orientation == BlockFace.SOUTH) {
                    loc.add(x, y, 0);
                } else {
                    loc.add(0, y, x);
                }
                Material type = loc.getBlock().getType();
                if (type != Material.AIR && type != Material.NETHER_PORTAL) {
                    return false;
                }
            }
        }

        return true;
    }

    private void createPortal(Location corner, BlockFace orientation) {
        World world = corner.getWorld();
        if (world == null) return;

        // Find the actual dimensions
        int width = 0;
        int height = 0;

        for (int w = 4; w <= 23; w++) {
            for (int h = 5; h <= 23; h++) {
                if (checkPortalFrame(corner, w, h, orientation)) {
                    width = w;
                    height = h;
                    break;
                }
            }
            if (width > 0) break;
        }

        // Fill interior with nether portal blocks
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                Location loc = corner.clone();
                if (orientation == BlockFace.SOUTH) {
                    loc.add(x, y, 0);
                } else {
                    loc.add(0, y, x);
                }
                Block portalBlock = loc.getBlock();
                portalBlock.setType(Material.NETHER_PORTAL);

                // Set the axis orientation of the portal
                if (portalBlock.getBlockData() instanceof org.bukkit.block.data.Orientable orientable) {
                    if (orientation == BlockFace.SOUTH) {
                        // Portal faces X-axis so the flat side aligns with the Z-axis frame
                        orientable.setAxis(org.bukkit.Axis.X);
                    } else {
                        // Portal faces Z-axis so the flat side aligns with the X-axis frame
                        orientable.setAxis(org.bukkit.Axis.Z);
                    }
                    portalBlock.setBlockData(orientable);
                }
            }
        }

        // Play sound
        world.playSound(corner, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            return;
        }

        Block block = event.getFrom().getBlock();

        // Check if portal frame is reinforced deepslate
        if (isWarpedPortal(block)) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            World currentWorld = player.getWorld();

            // INSTANT teleportation - no scheduler delay
            Location portalEntrance = event.getFrom();

            plugin.getLogger().info("=== INSTANT PORTAL TELEPORT ===");
            plugin.getLogger().info("Player: " + player.getName());
            plugin.getLogger().info("From World: " + currentWorld.getName());
            plugin.getLogger().info("From Location: " + portalEntrance.toString());

            if (currentWorld.getName().equals("warped_dimension")) {
                // Return to overworld
                World overworld = Bukkit.getWorld("world");
                if (overworld != null) {
                    plugin.getLogger().info("Teleporting to OVERWORLD");
                    Location destination = findOrCreateLinkedPortalInstant(portalEntrance, overworld);
                    plugin.getLogger().info("Final Destination: " + destination.toString());
                    player.teleport(destination);
                    player.sendMessage("§aReturned to the Overworld!");
                }
            } else {
                // Go to warped dimension
                World warpedWorld = WarpedWorldManager.getOrCreateWarpedWorld(plugin);
                if (warpedWorld != null) {
                    plugin.getLogger().info("Teleporting to WARPED DIMENSION");
                    Location destination = findOrCreateLinkedPortalInstant(portalEntrance, warpedWorld);
                    plugin.getLogger().info("Final Destination: " + destination.toString());
                    player.teleport(destination);
                    player.sendMessage("§5Welcome to the Warped Dimension!");
                }
            }
            plugin.getLogger().info("=== END INSTANT PORTAL TELEPORT ===");
        }
    }

    private Location findOrCreateLinkedPortalInstant(Location fromPortal, World toWorld) {
        // ULTRA-FAST version for instant teleportation
        Location targetLoc = new Location(
            toWorld,
            fromPortal.getX(),
            fromPortal.getY(),
            fromPortal.getZ(),
            fromPortal.getYaw(),
            fromPortal.getPitch()
        );

        plugin.getLogger().info("--- INSTANT PORTAL SEARCH ---");
        plugin.getLogger().info("Target: " + targetLoc.toString());

        // STEP 1: Registry check (instant)
        Location registryPortal = findPortalInRegistry(targetLoc, toWorld.getName());
        if (registryPortal != null) {
            plugin.getLogger().info("✓ Registry hit: " + registryPortal.toString());
            return registryPortal;
        }

        // STEP 2: Ultra-fast targeted search (minimal chunk loading)
        int chunkX = targetLoc.getBlockX() >> 4;
        int chunkZ = targetLoc.getBlockZ() >> 4;

        // Only ensure target chunk is loaded
        if (!toWorld.isChunkLoaded(chunkX, chunkZ)) {
            toWorld.loadChunk(chunkX, chunkZ);
        }

        // Very targeted search around exact coordinates
        Location instantPortal = findNearbyWarpedPortalInstant(targetLoc, 5); // Only 5 block radius
        if (instantPortal != null) {
            plugin.getLogger().info("✓ Instant search hit: " + instantPortal.toString());
            registerPortal(instantPortal, toWorld.getName());
            return instantPortal;
        }

        // STEP 3: If no instant hit, fall back to regular search (but still same tick)
        plugin.getLogger().info("No instant hit, using fast search...");
        return findOrCreateLinkedPortal(fromPortal, toWorld);
    }

    private Location findOrCreateLinkedPortal(Location fromPortal, World toWorld) {
        // Calculate corresponding coordinates (1:1 ratio, not like nether's 8:1)
        Location targetLoc = new Location(
            toWorld,
            fromPortal.getX(),
            fromPortal.getY(),
            fromPortal.getZ(),
            fromPortal.getYaw(),
            fromPortal.getPitch()
        );

        plugin.getLogger().info("--- FAST LINKED PORTAL SEARCH ---");
        plugin.getLogger().info("From Portal: " + fromPortal.toString());
        plugin.getLogger().info("Target Location: " + targetLoc.toString());
        plugin.getLogger().info("Target World: " + toWorld.getName());

        // STEP 1: Check registry FIRST (fastest option)
        plugin.getLogger().info("Step 1: Checking portal registry...");
        Location registryPortal = findPortalInRegistry(targetLoc, toWorld.getName());
        if (registryPortal != null) {
            plugin.getLogger().info("✓ Found portal in registry: " + registryPortal.toString());
            plugin.getLogger().info("--- END FAST PORTAL SEARCH (REGISTRY) ---");
            return registryPortal;
        }

        // STEP 2: Quick targeted search (smaller radius, faster)
        plugin.getLogger().info("Step 2: Quick search within 8 blocks...");
        int chunkX = targetLoc.getBlockX() >> 4;
        int chunkZ = targetLoc.getBlockZ() >> 4;

        // Only load the target chunk for quick search
        if (!toWorld.isChunkLoaded(chunkX, chunkZ)) {
            toWorld.loadChunk(chunkX, chunkZ);
            plugin.getLogger().info("Loaded target chunk for quick search: " + chunkX + ", " + chunkZ);
        }

        Location quickPortal = findNearbyWarpedPortal(targetLoc, 8);
        if (quickPortal != null) {
            plugin.getLogger().info("✓ Found portal in quick search: " + quickPortal.toString());
            registerPortal(quickPortal, toWorld.getName());
            plugin.getLogger().info("--- END FAST PORTAL SEARCH (QUICK) ---");
            return quickPortal;
        }

        // STEP 3: Extensive search only if quick search fails (slower but thorough)
        plugin.getLogger().info("Step 3: Extensive search within 20 blocks...");

        // Load additional chunks for extensive search
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                if (!toWorld.isChunkLoaded(x, z)) {
                    toWorld.loadChunk(x, z);
                    plugin.getLogger().info("Loaded chunk for extensive search: " + x + ", " + z);
                }
            }
        }

        Location extensivePortal = findNearbyWarpedPortal(targetLoc, 20);
        if (extensivePortal != null) {
            plugin.getLogger().info("✓ Found portal in extensive search: " + extensivePortal.toString());
            registerPortal(extensivePortal, toWorld.getName());
            plugin.getLogger().info("--- END FAST PORTAL SEARCH (EXTENSIVE) ---");
            return extensivePortal;
        }

        // No portal found, create one at a safe location
        plugin.getLogger().info("✗ No existing portal found, creating new portal");
        plugin.getLogger().info("--- END FAST PORTAL SEARCH (CREATING) ---");
        return createLinkedPortal(targetLoc);
    }

    private Location findNearbyWarpedPortal(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return null;

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        // Adjust Y-range based on search radius (smaller radius = smaller Y-range for speed)
        int yRange = (radius <= 8) ? 8 : 15;
        int startY = Math.max(world.getMinHeight(), centerY - yRange);
        int endY = Math.min(world.getMaxHeight() - 1, centerY + yRange);

        // Track the best portal found (closest to center)
        Location bestPortal = null;
        double bestDistance = Double.MAX_VALUE;
        int portalsFound = 0;
        int validPortals = 0;

        // Optimize search: check exact target Y first, then spiral outward
        int[] yOffsets = new int[yRange + 1];
        yOffsets[0] = 0; // exact center Y
        for (int i = 1; i <= yRange; i++) {
            yOffsets[i] = i;
        }

        // Search for existing portals
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                // Check Y levels in order of likelihood (exact first, then close, then far)
                for (int yOffset : yOffsets) {
                    for (int ySign = -1; ySign <= 1; ySign += 2) {
                        int y = centerY + (yOffset * ySign);
                        if (y < startY || y > endY) continue;

                        // Skip if chunk not loaded (for quick searches)
                        if (radius <= 8 && !world.isChunkLoaded(x >> 4, z >> 4)) {
                            continue;
                        }

                        Location loc = new Location(world, x, y, z);
                        Block block = loc.getBlock();

                        if (block.getType() == Material.NETHER_PORTAL) {
                            portalsFound++;

                            // Found a portal block, check if it has reinforced deepslate nearby
                            if (hasReinforcedDeepslateNearby(block)) {
                                validPortals++;

                                // Calculate distance from center
                                double distance = Math.sqrt(
                                    Math.pow(x - centerX, 2) +
                                    Math.pow(z - centerZ, 2) +
                                    Math.pow((y - centerY) / 3.0, 2) // Weight Y distance less
                                );

                                if (distance < bestDistance) {
                                    bestDistance = distance;
                                    bestPortal = loc.clone().add(0.5, 0, 0.5);
                                    if (radius <= 8) { // Only log for quick searches
                                        plugin.getLogger().info("Quick search found portal at " + loc.toString() + ", distance: " + distance);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (radius <= 8) { // Only log summary for quick searches
            plugin.getLogger().info("Quick portal search complete: " + portalsFound + " total portals, " + validPortals + " valid portals");
        }

        if (bestPortal != null && radius <= 8) {
            plugin.getLogger().info("✓ Quick search found best portal at distance " + bestDistance);
        }

        return bestPortal;
    }

    private Location findNearbyWarpedPortalInstant(Location center, int radius) {
        // ULTRA-FAST version for instant teleportation - minimal radius, optimized search
        World world = center.getWorld();
        if (world == null) return null;

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        // Only search exact target Y level first, then ±1, then ±2
        int[] yChecks = {0, 1, -1, 2, -2};

        Location bestPortal = null;
        double bestDistanceSquared = Double.MAX_VALUE;

        // Very tight search around target coordinates
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                // Check Y levels in order of probability
                for (int yOffset : yChecks) {
                    int y = centerY + yOffset;
                    if (y < world.getMinHeight() || y > world.getMaxHeight() - 1) continue;

                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();

                    if (block.getType() == Material.NETHER_PORTAL) {
                        // Found a portal block, check if it has reinforced deepslate nearby
                        if (hasReinforcedDeepslateNearby(block)) {
                            // Calculate squared distance (no expensive sqrt)
                            double distanceSquared = (x - centerX) * (x - centerX) +
                                                   (z - centerZ) * (z - centerZ) +
                                                   yOffset * yOffset;

                            if (distanceSquared < bestDistanceSquared) {
                                bestDistanceSquared = distanceSquared;
                                bestPortal = loc.clone().add(0.5, 0, 0.5);
                            }
                        }
                    }
                }
            }
        }

        return bestPortal;
    }

    private Location findPortalInRegistry(Location targetLoc, String worldName) {
        List<Location> portals = portalRegistry.get(worldName);
        if (portals == null || portals.isEmpty()) {
            return null; // Fast exit for empty registry
        }

        // Ultra-fast: find closest portal using squared distance (no sqrt)
        Location closest = null;
        double closestDistanceSquared = Double.MAX_VALUE;
        double thresholdSquared = 25 * 25; // 25 block threshold squared

        for (Location portal : portals) {
            double dx = portal.getX() - targetLoc.getX();
            double dy = (portal.getY() - targetLoc.getY()) / 3.0;
            double dz = portal.getZ() - targetLoc.getZ();
            double distanceSquared = dx*dx + dy*dy + dz*dz;

            if (distanceSquared < closestDistanceSquared && distanceSquared <= thresholdSquared) {
                closestDistanceSquared = distanceSquared;
                closest = portal;
            }
        }

        return closest;
    }

    private void registerPortal(Location portalLocation, String worldName) {
        List<Location> portals = portalRegistry.computeIfAbsent(worldName, k -> new ArrayList<>());

        plugin.getLogger().info("Registering portal for world '" + worldName + "': " + portalLocation.toString());
        plugin.getLogger().info("Current portals in registry: " + portals.size());

        // Remove any existing portals that are too close (within 5 blocks)
        final int[] removedCount = {0};
        portals.removeIf(existing -> {
            double distance = Math.sqrt(
                Math.pow(existing.getX() - portalLocation.getX(), 2) +
                Math.pow(existing.getZ() - portalLocation.getZ(), 2) +
                Math.pow((existing.getY() - portalLocation.getY()) / 3.0, 2)
            );
            if (distance <= 5) {
                plugin.getLogger().info("Removing duplicate portal at distance " + distance + ": " + existing.toString());
                removedCount[0]++;
                return true;
            }
            return false;
        });

        if (removedCount[0] > 0) {
            plugin.getLogger().info("Removed " + removedCount[0] + " duplicate portals");
        }

        // Add the new portal
        portals.add(portalLocation.clone());
        plugin.getLogger().info("✓ Portal registered. Total portals for '" + worldName + "': " + portals.size());
    }

    private boolean hasReinforcedDeepslateNearby(Block portalBlock) {
        // Check nearby blocks for reinforced deepslate (portal frame)
        // Ultra-optimized: check most likely positions first, early exit

        // Check immediate adjacent blocks (most common portal frame positions)
        for (int x = -1; x <= 1; x += 2) { // Only check -1 and 1 (not 0)
            for (int z = -1; z <= 1; z += 2) { // Only check -1 and 1 (not 0)
                Block nearby = portalBlock.getRelative(x, 0, z);
                if (nearby.getType() == Material.REINFORCED_DEEPSLATE) {
                    return true;
                }
            }
        }

        // Check above and below
        for (int y = -1; y <= 1; y += 2) { // Only check -1 and 1
            Block nearby = portalBlock.getRelative(0, y, 0);
            if (nearby.getType() == Material.REINFORCED_DEEPSLATE) {
                return true;
            }
        }

        // If not found in immediate area, check corners and extended range
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip center
                    Block nearby = portalBlock.getRelative(x, y, z);
                    if (nearby.getType() == Material.REINFORCED_DEEPSLATE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Location createLinkedPortal(Location targetLoc) {
        World world = targetLoc.getWorld();
        if (world == null) return targetLoc;

        // Find a safe location to build the portal
        Location buildLoc = findSafeBuildLocation(targetLoc);

        // Build a standard 4x5 portal frame
        int width = 4;
        int height = 5;

        Location corner = buildLoc.clone();

        // Build the frame
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Location loc = corner.clone().add(x, y, 0);

                // Bottom and top edges
                if (y == 0 || y == height - 1) {
                    loc.getBlock().setType(Material.REINFORCED_DEEPSLATE);
                }
                // Left and right edges
                else if (x == 0 || x == width - 1) {
                    loc.getBlock().setType(Material.REINFORCED_DEEPSLATE);
                }
                // Interior - portal blocks
                else {
                    Block portalBlock = loc.getBlock();
                    portalBlock.setType(Material.NETHER_PORTAL);

                    // Set axis orientation (X axis for portals facing the Z direction)
                    if (portalBlock.getBlockData() instanceof org.bukkit.block.data.Orientable orientable) {
                        orientable.setAxis(org.bukkit.Axis.X);
                        portalBlock.setBlockData(orientable);
                    }

                    // Ensure the portal block is properly updated
                    portalBlock.getState().update(true, false);
                }
            }
        }

        // Create portal particles and effects for visual feedback
        world.playSound(buildLoc, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);

        // Schedule a task to ensure portal blocks are properly formed
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Double-check portal blocks are properly set
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    Location loc = corner.clone().add(x, y, 0);
                    Block portalBlock = loc.getBlock();
                    if (portalBlock.getType() == Material.NETHER_PORTAL) {
                        if (portalBlock.getBlockData() instanceof org.bukkit.block.data.Orientable orientable) {
                            orientable.setAxis(org.bukkit.Axis.X);
                            portalBlock.setBlockData(orientable);
                        }
                        portalBlock.getState().update(true, false);
                    }
                }
            }
            world.playSound(buildLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.0f);
        }, 1L);

        // Register the new portal
        Location portalCenter = corner.clone().add(width / 2.0, 1, 0.5);
        registerPortal(portalCenter, world.getName());

        // Return the center of the portal for teleportation
        return portalCenter;
    }

    private Location findSafeBuildLocation(Location target) {
        World world = target.getWorld();
        if (world == null) return target;

        int x = target.getBlockX();
        int z = target.getBlockZ();

        // Try to find solid ground near the target Y level
        for (int yOffset = 0; yOffset <= 20; yOffset++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                int y = target.getBlockY() + (yOffset * sign);

                if (y < world.getMinHeight() || y > world.getMaxHeight() - 10) {
                    continue;
                }

                Location testLoc = new Location(world, x, y, z);

                // Check if there's solid ground and enough space above
                if (isSafeForPortal(testLoc)) {
                    return testLoc;
                }
            }
        }

        // If no safe location found, try to find a safe spot above the highest block
        int highestY = world.getHighestBlockYAt(x, z);

        // Check if the area above the highest block is safe (no water)
        for (int y = highestY + 1; y < Math.min(world.getMaxHeight() - 5, highestY + 15); y++) {
            Location testLoc = new Location(world, x, y, z);
            if (isSafeForPortal(testLoc)) {
                return testLoc;
            }
        }

        // If still no safe location, try to find ground above sea level that's not water
        for (int y = Math.max(63, world.getMinHeight() + 10); y < world.getMaxHeight() - 5; y++) {
            Location testLoc = new Location(world, x, y, z);

            // Check if there's solid ground below and the area is clear
            Material below = testLoc.clone().subtract(0, 1, 0).getBlock().getType();
            if (below.isSolid() && isSafeForPortal(testLoc)) {
                return testLoc;
            }
        }

        // Last resort: return a safe location above sea level
        return new Location(world, x, Math.max(65, highestY + 2), z);
    }

    private boolean isSafeForPortal(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        // Check if there's solid ground below (not water or lava)
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (!below.isSolid() || below == Material.WATER || below == Material.LAVA) {
            return false;
        }

        // Check if the ground itself is not water or lava
        Material ground = loc.getBlock().getType();
        if (ground == Material.WATER || ground == Material.LAVA) {
            return false;
        }

        // Check if there's enough vertical space (5 blocks high, 4 blocks wide)
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                Location checkLoc = loc.clone().add(x, y, 0);
                Material mat = checkLoc.getBlock().getType();

                // Only allow air and certain replaceable blocks, no water or lava
                if (mat != Material.AIR && !mat.name().contains("GRASS") &&
                    !mat.name().contains("ROOTS") && !mat.name().contains("VINE")) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isWarpedPortal(Block block) {
        // Check if nearby blocks contain reinforced deepslate (portal frame)
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    Block nearby = block.getRelative(x, y, z);
                    if (nearby.getType() == Material.REINFORCED_DEEPSLATE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
