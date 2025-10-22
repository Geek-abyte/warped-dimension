package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WarpedChorusFlowerPopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Only populate in the warped dimension
        if (!worldInfo.getName().equals("warped_dimension")) {
            return;
        }

        // Check if this chunk should have chorus flowers
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        
        // Use the biome provider to determine biome type
        WarpedBiomeProvider biomeProvider = new WarpedBiomeProvider();
        WarpedBiomes biomeType = biomeProvider.getWarpedBiomeType(centerX, centerZ, worldInfo.getSeed());

        if (biomeType != WarpedBiomes.AMETHYST_FOREST) {
            return;
        }

        // Generate 2-5 chorus flowers per chunk
        int flowerCount = 2 + random.nextInt(4);
        
        for (int i = 0; i < flowerCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = findHighestBlock(limitedRegion, x, z, worldInfo);

            if (canGenerateChorusFlower(limitedRegion, x, y, z)) {
                generateChorusFlower(limitedRegion, x, y, z, random);
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

    private boolean canGenerateChorusFlower(LimitedRegion region, int x, int y, int z) {
        if (!region.isInRegion(x, y - 1, z) || !region.isInRegion(x, y, z)) {
            return false;
        }

        Material ground = region.getType(x, y - 1, z);
        Material air = region.getType(x, y, z);

        // Must be on sculk with air above
        return ground == Material.SCULK && air == Material.AIR;
    }

    private void generateChorusFlower(LimitedRegion region, int x, int y, int z, Random random) {
        // Place end stone block first
        if (region.isInRegion(x, y - 1, z)) {
            region.setType(x, y - 1, z, Material.END_STONE);
        }
        
        // Place chorus flower on top
        if (region.isInRegion(x, y, z)) {
            region.setType(x, y, z, Material.CHORUS_FLOWER);
        }
        
        // Occasionally add a small chorus plant structure
        if (random.nextDouble() < 0.3) { // 30% chance
            generateChorusPlant(region, x, y, z, random);
        }
    }

    private void generateChorusPlant(LimitedRegion region, int x, int y, int z, Random random) {
        // Generate a small chorus plant structure (2-4 blocks tall)
        int height = 2 + random.nextInt(3);
        
        for (int i = 1; i <= height; i++) {
            if (region.isInRegion(x, y + i, z)) {
                Material currentType = region.getType(x, y + i, z);
                if (currentType == Material.AIR) {
                    region.setType(x, y + i, z, Material.CHORUS_PLANT);
                }
            }
        }
        
        // Add chorus flower at the top
        if (region.isInRegion(x, y + height, z)) {
            Material currentType = region.getType(x, y + height, z);
            if (currentType == Material.CHORUS_PLANT) {
                region.setType(x, y + height, z, Material.CHORUS_FLOWER);
            }
        }
    }
}
