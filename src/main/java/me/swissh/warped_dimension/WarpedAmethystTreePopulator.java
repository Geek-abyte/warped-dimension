package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WarpedAmethystTreePopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Only populate in the warped dimension
        if (!worldInfo.getName().equals("warped_dimension")) {
            return;
        }

        // Check if this chunk should have amethyst forest trees
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        
        // Use the biome provider to determine biome type
        WarpedBiomeProvider biomeProvider = new WarpedBiomeProvider();
        WarpedBiomes biomeType = biomeProvider.getWarpedBiomeType(centerX, centerZ, worldInfo.getSeed());

        if (biomeType != WarpedBiomes.AMETHYST_FOREST) {
            return;
        }

        // Generate 2-4 amethyst trees per chunk (more trees since they're shorter)
        int treeCount = 2 + random.nextInt(3);
        
        for (int i = 0; i < treeCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = findHighestBlock(limitedRegion, x, z, worldInfo);

            if (canGenerateTree(limitedRegion, x, y, z)) {
                generateAmethystTree(limitedRegion, x, y, z, random);
            }
        }
    }

    private int findHighestBlock(LimitedRegion region, int x, int z, WorldInfo worldInfo) {
        for (int y = worldInfo.getMaxHeight() - 1; y > worldInfo.getMinHeight(); y--) {
            if (region.isInRegion(x, y, z)) {
                Material blockType = region.getType(x, y, z);
                if (blockType != Material.AIR) {
                    return y + 1;
                }
            }
        }
        return worldInfo.getMinHeight();
    }

    private boolean canGenerateTree(LimitedRegion region, int x, int y, int z) {
        if (!region.isInRegion(x, y - 1, z) || !region.isInRegion(x, y, z) || 
            !region.isInRegion(x, y + 1, z) || !region.isInRegion(x, y + 2, z)) {
            return false;
        }

        Material ground = region.getType(x, y - 1, z);
        Material air1 = region.getType(x, y, z);
        Material air2 = region.getType(x, y + 1, z);
        Material air3 = region.getType(x, y + 2, z);

        // Must be on sculk
        if (ground != Material.SCULK) {
            return false;
        }

        // Must have air space above
        return air1 == Material.AIR && air2 == Material.AIR && air3 == Material.AIR;
    }

    private void generateAmethystTree(LimitedRegion region, int x, int y, int z, Random random) {
        // Tree height: 4-7 blocks (shorter, more shrub-like)
        int height = 4 + random.nextInt(4);
        
        // Generate trunk (dark oak wood)
        for (int i = 0; i < height; i++) {
            if (region.isInRegion(x, y + i, z)) {
                region.setType(x, y + i, z, Material.DARK_OAK_LOG);
            }
        }

        // Generate amethyst canopy
        generateAmethystCanopy(region, x, y + height, z, random);
        
        // Add amethyst clusters on canopy
        addAmethystClusters(region, x, y + height, z, random);
    }

    private void generateAmethystCanopy(LimitedRegion region, int x, int y, int z, Random random) {
        // Canopy radius: 2-4 blocks (smaller for shorter trees)
        int radius = 2 + random.nextInt(3);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= 2; dy++) { // Smaller canopy height
                    int distance = Math.abs(dx) + Math.abs(dz);
                    
                    // Create cloud-like canopy shape
                    if (distance <= radius && (dy == 0 || random.nextDouble() < 0.7)) {
                        int blockX = x + dx;
                        int blockY = y + dy;
                        int blockZ = z + dz;
                        
                        if (region.isInRegion(blockX, blockY, blockZ)) {
                            Material currentType = region.getType(blockX, blockY, blockZ);
                            
                            if (currentType == Material.AIR) {
                                region.setType(blockX, blockY, blockZ, Material.AMETHYST_BLOCK);
                                
                                // Add white froglights within the canopy for internal glow
                                if (random.nextDouble() < 0.15) { // 15% chance for froglights
                                    region.setType(blockX, blockY, blockZ, Material.OCHRE_FROGLIGHT);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addAmethystClusters(LimitedRegion region, int x, int y, int z, Random random) {
        // Add small amethyst clusters on the sides and tops of canopies
        for (int i = 0; i < 3; i++) {
            int clusterX = x + random.nextInt(7) - 3;
            int clusterY = y + random.nextInt(3);
            int clusterZ = z + random.nextInt(7) - 3;
            
            if (region.isInRegion(clusterX, clusterY, clusterZ)) {
                Material currentType = region.getType(clusterX, clusterY, clusterZ);
                
                // Place clusters on amethyst blocks
                if (currentType == Material.AMETHYST_BLOCK) {
                    // Add small amethyst clusters
                    region.setType(clusterX, clusterY, clusterZ, Material.AMETHYST_CLUSTER);
                }
            }
        }
    }
}
