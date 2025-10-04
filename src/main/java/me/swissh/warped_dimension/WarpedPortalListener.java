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

public class WarpedPortalListener implements Listener {

    private final Warped_dimension plugin;

    public WarpedPortalListener(Warped_dimension plugin) {
        this.plugin = plugin;
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
            player.sendMessage(ChatColor.DARK_AQUA + "Warped Portal activated!");
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
            Location portalEntrance = event.getFrom();

            if (currentWorld.getName().equals("warped_dimension")) {
                // Return to overworld
                World overworld = Bukkit.getWorld("world");
                if (overworld != null) {
                    Location destination = findOrCreateLinkedPortal(portalEntrance, overworld);
                    player.teleport(destination);
                    player.sendMessage(ChatColor.GREEN + "Returned to the Overworld!");
                }
            } else {
                // Go to warped dimension
                World warpedWorld = WarpedWorldManager.getOrCreateWarpedWorld(plugin);
                if (warpedWorld != null) {
                    Location destination = findOrCreateLinkedPortal(portalEntrance, warpedWorld);
                    player.teleport(destination);
                    player.sendMessage(ChatColor.DARK_AQUA + "Welcome to the Warped Dimension!");
                }
            }
        }
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

        // Ensure the target chunk is loaded before searching
        int chunkX = targetLoc.getBlockX() >> 4;
        int chunkZ = targetLoc.getBlockZ() >> 4;

        // Load chunks in a small area around the target location
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                if (!toWorld.isChunkLoaded(x, z)) {
                    toWorld.loadChunk(x, z);
                }
            }
        }

        // Search for existing portal nearby (within 16 blocks)
        Location existingPortal = findNearbyWarpedPortal(targetLoc, 16);
        if (existingPortal != null) {
            return existingPortal;
        }

        // No portal found, create one
        return createLinkedPortal(targetLoc);
    }

    private Location findNearbyWarpedPortal(Location center, int radius) {
        World world = center.getWorld();
        if (world == null) return null;

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        // Search for existing portals
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = Math.max(world.getMinHeight(), centerY - 10);
                     y <= Math.min(world.getMaxHeight() - 1, centerY + 10); y++) {

                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();

                    if (block.getType() == Material.NETHER_PORTAL) {
                        // Found a portal block, check if it has reinforced deepslate nearby
                        if (hasReinforcedDeepslateNearby(block)) {
                            // Return the center of the portal
                            return loc.clone().add(0.5, 0, 0.5);
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean hasReinforcedDeepslateNearby(Block portalBlock) {
        // Check nearby blocks for reinforced deepslate (portal frame)
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
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

                    // Set axis orientation
                    if (portalBlock.getBlockData() instanceof org.bukkit.block.data.Orientable orientable) {
                        orientable.setAxis(org.bukkit.Axis.X);
                        portalBlock.setBlockData(orientable);
                    }
                }
            }
        }

        // Play sound
        world.playSound(buildLoc, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);

        // Return the center of the portal for teleportation
        return corner.clone().add(width / 2.0, 1, 0.5);
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

        // If no safe location found, use highest block
        int highestY = world.getHighestBlockYAt(x, z);
        return new Location(world, x, highestY + 1, z);
    }

    private boolean isSafeForPortal(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        // Check if there's solid ground below
        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (!below.isSolid()) {
            return false;
        }

        // Check if there's enough vertical space (5 blocks high, 4 blocks wide)
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
                Location checkLoc = loc.clone().add(x, y, 0);
                Material mat = checkLoc.getBlock().getType();

                // Allow air and replaceable blocks
                if (mat != Material.AIR && mat != Material.WATER &&
                    mat != Material.LAVA && !mat.name().contains("GRASS") &&
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
