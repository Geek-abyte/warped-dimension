package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WarpedSculkTreePopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Only populate in the warped dimension
        if (!worldInfo.getName().equals("warped_dimension")) {
            return;
        }

        // Check if this chunk should have sculk forest trees
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        
        // Use the biome provider to determine biome type
        WarpedBiomeProvider biomeProvider = new WarpedBiomeProvider();
        WarpedBiomes biomeType = biomeProvider.getWarpedBiomeType(centerX, centerZ, worldInfo.getSeed());

        if (biomeType != WarpedBiomes.SCULK_FOREST) {
            return;
        }

        // Generate 1-3 sculk trees per chunk
        int treeCount = 1 + random.nextInt(3);
        
        for (int i = 0; i < treeCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = findHighestBlock(limitedRegion, x, z, worldInfo);

            if (canGenerateTree(limitedRegion, x, y, z)) {
                generateSculkTree(limitedRegion, x, y, z, random);
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

        // Must be on warped nylium or sculk
        if (ground != Material.WARPED_NYLIUM && ground != Material.SCULK) {
            return false;
        }

        // Must have air space above
        return air1 == Material.AIR && air2 == Material.AIR && air3 == Material.AIR;
    }

    private void generateSculkTree(LimitedRegion region, int x, int y, int z, Random random) {
        // Tree height: 6-12 blocks
        int height = 6 + random.nextInt(7);
        
        // Generate trunk (mix of warped stem and end stone)
        for (int i = 0; i < height; i++) {
            if (region.isInRegion(x, y + i, z)) {
                // 70% warped stem, 30% end stone for texture variation
                if (random.nextDouble() < 0.7) {
                    region.setType(x, y + i, z, Material.WARPED_STEM);
                } else {
                    region.setType(x, y + i, z, Material.END_STONE);
                }
            }
        }

        // Generate canopy (sculk blocks with dried kelp blocks)
        generateCanopy(region, x, y + height, z, random);
        
        // Add sculk veins on trunk
        addSculkVines(region, x, y, z, height, random);
        
        // Very sparsely add sculk catalyst at base of largest trees
        if (random.nextDouble() < 0.05 && region.isInRegion(x, y - 1, z)) { // 5% chance - very sparse
            region.setType(x, y - 1, z, Material.SCULK_CATALYST);
        }
    }

    private void generateCanopy(LimitedRegion region, int x, int y, int z, Random random) {
        // Canopy radius: 3-5 blocks
        int radius = 3 + random.nextInt(3);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    int distance = Math.abs(dx) + Math.abs(dz);
                    
                    // Create organic canopy shape
                    if (distance <= radius && (dy == 0 || random.nextDouble() < 0.6)) {
                        int blockX = x + dx;
                        int blockY = y + dy;
                        int blockZ = z + dz;
                        
                        if (region.isInRegion(blockX, blockY, blockZ)) {
                            Material currentType = region.getType(blockX, blockY, blockZ);
                            
                            if (currentType == Material.AIR) {
                                // 80% sculk, 20% dried kelp blocks
                                if (random.nextDouble() < 0.8) {
                                    region.setType(blockX, blockY, blockZ, Material.SCULK);
                                } else {
                                    region.setType(blockX, blockY, blockZ, Material.DRIED_KELP_BLOCK);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Add sculk sensor at peak occasionally (as per description)
        if (random.nextDouble() < 0.15 && region.isInRegion(x, y + 2, z)) { // 15% chance - very occasional
            region.setType(x, y + 2, z, Material.SCULK_SENSOR);
        }
    }

    private void addSculkVines(LimitedRegion region, int x, int y, int z, int height, Random random) {
        // Add sculk veins crawling down the trunk
        for (int i = 0; i < height; i++) {
            if (random.nextDouble() < 0.4) { // 40% chance per level
                // Add sculk veins on sides of trunk
                int sideX = x + (random.nextBoolean() ? 1 : -1);
                int sideZ = z + (random.nextBoolean() ? 1 : -1);
                
                if (region.isInRegion(sideX, y + i, sideZ)) {
                    Material currentType = region.getType(sideX, y + i, sideZ);
                    if (currentType == Material.AIR) {
                        region.setType(sideX, y + i, sideZ, Material.SCULK_VEIN);
                    }
                }
            }
        }
    }
}
