package me.swissh.warped_dimension;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.Random;

public class WarpedCommand implements CommandExecutor {

    private final Warped_dimension plugin;

    public WarpedCommand(Warped_dimension plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        World warpedWorld = Bukkit.getWorld("warped_dimension");

        if (warpedWorld == null) {
            player.sendMessage("§cWarped Dimension world is not loaded!");
            return true;
        }

        // Handle subcommands
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("ruins") || args[0].equalsIgnoreCase("locate")) {
                return handleLocateRuins(player, warpedWorld);
            } else if (args[0].equalsIgnoreCase("help")) {
                return handleHelp(player);
            }
        }

        // Default: teleport to warped dimension
        Location spawnLocation = getSafeSpawnLocation(warpedWorld);

        // Play teleportation sound effect
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);

        // Teleport the player
        player.teleport(spawnLocation);

        // Play arrival sound
        player.playSound(spawnLocation, Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);
        player.sendMessage("§d§lWelcome to the Warped Dimension!");

        return true;
    }

    private boolean handleLocateRuins(Player player, World warpedWorld) {
        // Search for ruins in a smaller, more manageable area
        Location playerLoc = player.getLocation();
        int searchRadius = 500; // Reduced search radius for faster execution
        int chunkRadius = searchRadius / 16;
        
        player.sendMessage("§e§lSearching for warped ruins...");
        
        Location nearestRuins = null;
        double nearestDistance = Double.MAX_VALUE;
        
        // Limit the search to prevent long execution times
        int maxChunks = 50; // Maximum chunks to check
        int checkedChunks = 0;
        
        for (int chunkX = -chunkRadius; chunkX <= chunkRadius && checkedChunks < maxChunks; chunkX++) {
            for (int chunkZ = -chunkRadius; chunkZ <= chunkRadius && checkedChunks < maxChunks; chunkZ++) {
                checkedChunks++;
                
                int worldChunkX = (playerLoc.getBlockX() >> 4) + chunkX;
                int worldChunkZ = (playerLoc.getBlockZ() >> 4) + chunkZ;
                
                // Check if this chunk should have ruins (1% chance)
                long seed = warpedWorld.getSeed();
                Random random = new Random(seed + worldChunkX * 341873128712L + worldChunkZ * 132897987541L);
                
                if (random.nextDouble() < 0.01) { // 1% chance
                    // This chunk should have ruins
                    int centerX = worldChunkX * 16 + 8;
                    int centerZ = worldChunkZ * 16 + 8;
                    int centerY = 64; // Default Y level for ruins
                    
                    Location ruinsLocation = new Location(warpedWorld, centerX, centerY, centerZ);
                    double distance = playerLoc.distance(ruinsLocation);
                    
                    if (distance <= searchRadius && distance < nearestDistance) {
                        nearestRuins = ruinsLocation;
                        nearestDistance = distance;
                    }
                }
            }
        }
        
        if (nearestRuins != null) {
            // Found ruins nearby
            player.sendMessage("§a§lFound warped ruins at: §f" + 
                nearestRuins.getBlockX() + ", " + nearestRuins.getBlockY() + ", " + nearestRuins.getBlockZ() + 
                " §7(distance: " + String.format("%.1f", nearestDistance) + " blocks)");
            
            // Calculate relative coordinates
            int relX = nearestRuins.getBlockX() - playerLoc.getBlockX();
            int relY = nearestRuins.getBlockY() - playerLoc.getBlockY();
            int relZ = nearestRuins.getBlockZ() - playerLoc.getBlockZ();
            
            player.sendMessage("§eRelative coordinates: §f" + relX + ", " + relY + ", " + relZ);
            
            // Give direction hints
            String direction = getDirection(relX, relZ);
            player.sendMessage("§eDirection: §f" + direction);
            
            // Set compass target to the ruins
            player.setCompassTarget(nearestRuins);
            player.sendMessage("§eYour compass now points to the ruins!");
            
            return true;
        } else {
            player.sendMessage("§cNo warped ruins found within " + searchRadius + " blocks.");
            player.sendMessage("§7Try exploring further or use §f/warped §7to teleport to a new area.");
            return true;
        }
    }

    private boolean handleHelp(Player player) {
        player.sendMessage("§d§l=== Warped Dimension Commands ===");
        player.sendMessage("§f/warped §7- Teleport to the warped dimension");
        player.sendMessage("§f/warped ruins §7- Locate nearby warped ruins");
        player.sendMessage("§f/warped locate §7- Same as ruins command");
        player.sendMessage("§f/warped help §7- Show this help message");
        return true;
    }

    private String getDirection(int relX, int relZ) {
        if (relX == 0 && relZ == 0) {
            return "§cYou are standing on the ruins!";
        }
        
        StringBuilder direction = new StringBuilder();
        
        // Z direction (North/South)
        if (relZ < 0) {
            direction.append("§bNorth");
        } else if (relZ > 0) {
            direction.append("§dSouth");
        }
        
        // X direction (East/West)
        if (relX > 0) {
            if (direction.length() > 0) direction.append("§7-");
            direction.append("§eEast");
        } else if (relX < 0) {
            if (direction.length() > 0) direction.append("§7-");
            direction.append("§6West");
        }
        
        // Add distance approximation
        double distance = Math.sqrt(relX * relX + relZ * relZ);
        if (distance < 50) {
            direction.append(" §7(very close)");
        } else if (distance < 200) {
            direction.append(" §7(close)");
        } else if (distance < 500) {
            direction.append(" §7(moderate distance)");
        } else {
            direction.append(" §7(far)");
        }
        
        return direction.toString();
    }

    private Location getSafeSpawnLocation(World world) {
        Location spawn = world.getSpawnLocation();

        // Find the highest solid block at spawn
        int x = spawn.getBlockX();
        int z = spawn.getBlockZ();
        int y = world.getHighestBlockYAt(x, z);

        // Make sure we're on solid ground
        Location safeLocation = new Location(world, x + 0.5, y + 1, z + 0.5);

        // Verify the location is safe (solid ground below, air above)
        Material groundBlock = world.getBlockAt(x, y, z).getType();
        if (groundBlock == Material.AIR || groundBlock == Material.WATER || groundBlock == Material.LAVA) {
            // If not safe, try to find a safe spot nearby
            for (int radius = 1; radius <= 10; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        int checkY = world.getHighestBlockYAt(x + dx, z + dz);
                        Material checkGround = world.getBlockAt(x + dx, checkY, z + dz).getType();
                        if (checkGround.isSolid() && checkGround != Material.WATER && checkGround != Material.LAVA) {
                            return new Location(world, x + dx + 0.5, checkY + 1, z + dz + 0.5);
                        }
                    }
                }
            }
        }

        return safeLocation;
    }
}
