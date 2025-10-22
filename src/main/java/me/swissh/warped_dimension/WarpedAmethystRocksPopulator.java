package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WarpedAmethystRocksPopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Only populate in the warped dimension
        if (!worldInfo.getName().equals("warped_dimension")) {
            return;
        }

        // Check if this chunk should have amethyst rocks and shards
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        
        // Use the biome provider to determine biome type
        WarpedBiomeProvider biomeProvider = new WarpedBiomeProvider();
        WarpedBiomes biomeType = biomeProvider.getWarpedBiomeType(centerX, centerZ, worldInfo.getSeed());

        if (biomeType != WarpedBiomes.AMETHYST_FOREST) {
            return;
        }

        // Generate amethyst rocks and shards
        generateAmethystRocks(limitedRegion, chunkX, chunkZ, worldInfo, random);
        generateAmethystShards(limitedRegion, chunkX, chunkZ, worldInfo, random);
    }

    private void generateAmethystRocks(LimitedRegion region, int chunkX, int chunkZ, WorldInfo worldInfo, Random random) {
        // Generate 3-6 amethyst rocks per chunk
        int rockCount = 3 + random.nextInt(4);
        
        for (int i = 0; i < rockCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = findHighestBlock(region, x, z, worldInfo);

            if (canGenerateRock(region, x, y, z)) {
                generateRock(region, x, y, z, random);
            }
        }
    }

    private void generateAmethystShards(LimitedRegion region, int chunkX, int chunkZ, WorldInfo worldInfo, Random random) {
        // Generate 8-15 amethyst shards per chunk (as shrubs)
        int shardCount = 8 + random.nextInt(8);
        
        for (int i = 0; i < shardCount; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = findHighestBlock(region, x, z, worldInfo);

            if (canGenerateShard(region, x, y, z)) {
                generateShard(region, x, y, z, random);
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

    private boolean canGenerateRock(LimitedRegion region, int x, int y, int z) {
        if (!region.isInRegion(x, y - 1, z) || !region.isInRegion(x, y, z)) {
            return false;
        }

        Material ground = region.getType(x, y - 1, z);
        Material air = region.getType(x, y, z);

        // Must be on sculk with air above
        return ground == Material.SCULK && air == Material.AIR;
    }

    private boolean canGenerateShard(LimitedRegion region, int x, int y, int z) {
        if (!region.isInRegion(x, y - 1, z) || !region.isInRegion(x, y, z)) {
            return false;
        }

        Material ground = region.getType(x, y - 1, z);
        Material air = region.getType(x, y, z);

        // Must be on sculk with air above
        return ground == Material.SCULK && air == Material.AIR;
    }

    private void generateRock(LimitedRegion region, int x, int y, int z, Random random) {
        // Generate a small amethyst rock (2-4 blocks)
        int rockSize = 2 + random.nextInt(3);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy < rockSize; dy++) {
                    int blockX = x + dx;
                    int blockY = y + dy;
                    int blockZ = z + dz;
                    
                    if (region.isInRegion(blockX, blockY, blockZ)) {
                        Material currentType = region.getType(blockX, blockY, blockZ);
                        
                        if (currentType == Material.AIR && random.nextDouble() < 0.7) {
                            region.setType(blockX, blockY, blockZ, Material.AMETHYST_BLOCK);
                        }
                    }
                }
            }
        }
    }

    private void generateShard(LimitedRegion region, int x, int y, int z, Random random) {
        // Generate amethyst shards as small shrubs (1-2 blocks tall)
        int shardHeight = 1 + random.nextInt(2);
        
        for (int dy = 0; dy < shardHeight; dy++) {
            if (region.isInRegion(x, y + dy, z)) {
                Material currentType = region.getType(x, y + dy, z);
                
                if (currentType == Material.AIR) {
                    // Use different amethyst shard types for variety
                    Material shardType = random.nextBoolean() ? 
                        Material.AMETHYST_CLUSTER : Material.BUDDING_AMETHYST;
                    region.setType(x, y + dy, z, shardType);
                }
            }
        }
    }
}
