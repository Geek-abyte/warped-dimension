package me.swissh.warped_dimension;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WarpedTreePopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Generate warped trees randomly
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;

        // Try to place 2-4 warped fungi per chunk
        int treeCount = 2 + random.nextInt(3);

        for (int i = 0; i < treeCount; i++) {
            int x = worldX + random.nextInt(16);
            int z = worldZ + random.nextInt(16);

            // Find the highest block
            for (int y = worldInfo.getMaxHeight() - 1; y > worldInfo.getMinHeight(); y--) {
                if (limitedRegion.isInRegion(x, y, z)) {
                    Material blockType = limitedRegion.getType(x, y, z);

                    if (blockType == Material.WARPED_NYLIUM || blockType == Material.SCULK) {
                        // Place warped fungus and try to grow it
                        if (limitedRegion.isInRegion(x, y + 1, z)) {
                            limitedRegion.setType(x, y + 1, z, Material.WARPED_FUNGUS);

                            // Generate a huge warped fungus structure manually
                            generateWarpedTree(limitedRegion, x, y + 1, z, random);
                        }
                        break;
                    }
                }
            }
        }

        // Add some warped roots and fungi on the ground
        for (int i = 0; i < 10; i++) {
            int x = worldX + random.nextInt(16);
            int z = worldZ + random.nextInt(16);

            for (int y = worldInfo.getMaxHeight() - 1; y > worldInfo.getMinHeight(); y--) {
                if (limitedRegion.isInRegion(x, y, z) && limitedRegion.isInRegion(x, y + 1, z)) {
                    Material blockType = limitedRegion.getType(x, y, z);

                    if (blockType == Material.WARPED_NYLIUM || blockType == Material.SCULK) {
                        Material vegetation = random.nextBoolean() ? Material.WARPED_ROOTS : Material.WARPED_FUNGUS;
                        limitedRegion.setType(x, y + 1, z, vegetation);
                        break;
                    }
                }
            }
        }
    }

    private void generateWarpedTree(LimitedRegion region, int x, int y, int z, Random random) {
        int height = 10 + random.nextInt(10);

        // Generate trunk
        for (int i = 0; i < height; i++) {
            if (region.isInRegion(x, y + i, z)) {
                region.setType(x, y + i, z, Material.WARPED_STEM);
            }
        }

        // Generate canopy
        int canopyStart = y + height - 4;
        for (int dy = 0; dy < 6; dy++) {
            int radius = dy < 3 ? 2 : 1;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= radius) {
                        int leafY = canopyStart + dy;
                        if (region.isInRegion(x + dx, leafY, z + dz)) {
                            Material current = region.getType(x + dx, leafY, z + dz);
                            if (current == Material.AIR || current == Material.WARPED_ROOTS) {
                                region.setType(x + dx, leafY, z + dz, Material.WARPED_WART_BLOCK);
                            }
                        }
                    }
                }
            }
        }

        // Add shroomlights
        for (int i = 0; i < 3; i++) {
            int shroomY = canopyStart + random.nextInt(4);
            int shroomX = x + random.nextInt(3) - 1;
            int shroomZ = z + random.nextInt(3) - 1;

            if (region.isInRegion(shroomX, shroomY, shroomZ)) {
                if (region.getType(shroomX, shroomY, shroomZ) == Material.WARPED_WART_BLOCK) {
                    region.setType(shroomX, shroomY, shroomZ, Material.SHROOMLIGHT);
                }
            }
        }
    }
}

