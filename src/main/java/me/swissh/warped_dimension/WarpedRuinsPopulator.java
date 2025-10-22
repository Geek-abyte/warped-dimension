package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WarpedRuinsPopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Only populate in the warped dimension
        if (!worldInfo.getName().equals("warped_dimension")) {
            return;
        }

        // Check if this chunk should have ruins
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        
        // Use the biome provider to determine biome type
        WarpedBiomeProvider biomeProvider = new WarpedBiomeProvider();
        WarpedBiomes biomeType = biomeProvider.getWarpedBiomeType(centerX, centerZ, worldInfo.getSeed());

        // Only generate ruins in warped forest biome
        if (biomeType != WarpedBiomes.WARPED_FOREST) {
            return;
        }

        // 1% chance to generate ruins in each chunk (much rarer than villages)
        if (random.nextDouble() > 0.01) {
            return;
        }

        // Generate ruins
        generateWarpedRuins(limitedRegion, random, centerX, centerZ, worldInfo);
    }

    private void generateWarpedRuins(@NotNull LimitedRegion region, @NotNull Random random, int centerX, int centerZ, @NotNull WorldInfo worldInfo) {
        // Find a suitable location for the ruins with better terrain integration
        int ruinsY = findSuitableRuinsLocation(region, centerX, centerZ, worldInfo);
        if (ruinsY == -1) return;

        // Generate different types of ruins with terrain integration
        int ruinType = random.nextInt(8);
        switch (ruinType) {
            case 0:
                generateAncientTower(region, random, centerX, ruinsY, centerZ);
                break;
            case 1:
                generateBrokenTemple(region, random, centerX, ruinsY, centerZ);
                break;
            case 2:
                generateCrystalRuins(region, random, centerX, ruinsY, centerZ);
                break;
            case 3:
                generateWarpedFortress(region, random, centerX, ruinsY, centerZ);
                break;
            case 4:
                generateSculkMonument(region, random, centerX, ruinsY, centerZ);
                break;
            case 5:
                generateFloatingRuins(region, random, centerX, ruinsY, centerZ);
                break;
            case 6:
                generateUndergroundRuins(region, random, centerX, ruinsY, centerZ);
                break;
            case 7:
                generateCliffsideRuins(region, random, centerX, ruinsY, centerZ);
                break;
        }
    }

    private void generateAncientTower(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate a sophisticated partially collapsed tower with terrain integration
        int height = 6 + random.nextInt(8); // 6-13 blocks tall (smaller)
        int radius = 2 + random.nextInt(2); // 2-3 block radius (smaller)
        
        // Create foundation that follows terrain
        createFoundation(region, x, y, z, radius + 1);
        
        // Tower base with better integration
        for (int dy = 0; dy < height; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int blockX = x + dx;
                    int blockY = y + dy;
                    int blockZ = z + dz;
                    
                    if (!region.isInRegion(blockX, blockY, blockZ)) continue;
                    
                    // Create hollow tower with sophisticated damage patterns
                    if (dx == -radius || dx == radius || dz == -radius || dz == radius) {
                        double damageChance = 0.2 + (dy * 0.05); // More damage higher up
                        if (random.nextDouble() > damageChance) {
                            region.setType(blockX, blockY, blockZ, Material.BLACKSTONE);
                            
                            // Add some variety with different blackstone types
                            if (random.nextDouble() < 0.3) {
                                region.setType(blockX, blockY, blockZ, Material.POLISHED_BLACKSTONE);
                            }
                        }
                    }
                }
            }
        }
        
        // Add sophisticated warped elements
        for (int i = 0; i < 3; i++) {
            int offsetX = random.nextInt(radius * 2 + 1) - radius;
            int offsetZ = random.nextInt(radius * 2 + 1) - radius;
            if (region.isInRegion(x + offsetX, y + height - 1, z + offsetZ)) {
                region.setType(x + offsetX, y + height - 1, z + offsetZ, Material.WARPED_WART_BLOCK);
            }
        }
        
        // Add some strategic floating debris
        for (int i = 0; i < 4; i++) {
            int offsetX = random.nextInt(12) - 6;
            int offsetZ = random.nextInt(12) - 6;
            int offsetY = random.nextInt(3) + 1;
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.BLACKSTONE);
            }
        }
        
        // Add some warped roots growing on the tower
        for (int i = 0; i < 6; i++) {
            int offsetX = random.nextInt(radius * 2 + 1) - radius;
            int offsetZ = random.nextInt(radius * 2 + 1) - radius;
            int offsetY = random.nextInt(height);
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.WARPED_ROOTS);
            }
        }
    }

    private void generateBrokenTemple(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate a ruined temple structure
        int width = 8 + random.nextInt(8); // 8-15 blocks wide
        int length = 8 + random.nextInt(8); // 8-15 blocks long
        int height = 4 + random.nextInt(6); // 4-9 blocks tall
        
        // Temple base
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                for (int dy = 0; dy < height; dy++) {
                    int blockX = x + dx;
                    int blockY = y + dy;
                    int blockZ = z + dz;
                    
                    if (!region.isInRegion(blockX, blockY, blockZ)) continue;
                    
                    Material material;
                    if (dy == 0) {
                        material = Material.BLACKSTONE;
                    } else if (dy == height - 1) {
                        // Roof - mostly missing
                        if (random.nextDouble() > 0.6) {
                            material = Material.BLACKSTONE;
                        } else {
                            material = Material.AIR;
                        }
                    } else if (dx == 0 || dx == width - 1 || dz == 0 || dz == length - 1) {
                        // Walls - partially collapsed
                        if (random.nextDouble() > 0.4) {
                            material = Material.BLACKSTONE;
                        } else {
                            material = Material.AIR;
                        }
                    } else {
                        material = Material.AIR;
                    }
                    
                    region.setType(blockX, blockY, blockZ, material);
                }
            }
        }
        
        // Add some warped columns
        for (int i = 0; i < 4; i++) {
            int colX = x + (i % 2) * (width - 1);
            int colZ = z + (i / 2) * (length - 1);
            for (int dy = 0; dy < height; dy++) {
                if (region.isInRegion(colX, y + dy, colZ)) {
                    region.setType(colX, y + dy, colZ, Material.WARPED_STEM);
                }
            }
        }
        
        // Add some scattered debris
        for (int i = 0; i < 15; i++) {
            int offsetX = random.nextInt(width + 4) - 2;
            int offsetZ = random.nextInt(length + 4) - 2;
            if (region.isInRegion(x + offsetX, y, z + offsetZ)) {
                region.setType(x + offsetX, y, z + offsetZ, Material.BLACKSTONE);
            }
        }
    }

    private void generateCrystalRuins(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate ruins with amethyst crystals
        int size = 6 + random.nextInt(6); // 6-11 blocks
        
        // Base structure
        for (int dx = -size/2; dx <= size/2; dx++) {
            for (int dz = -size/2; dz <= size/2; dz++) {
                int blockX = x + dx;
                int blockZ = z + dz;
                
                if (!region.isInRegion(blockX, y, blockZ)) continue;
                
                if (random.nextDouble() > 0.7) {
                    region.setType(blockX, y, blockZ, Material.BLACKSTONE);
                }
            }
        }
        
        // Add amethyst crystals
        for (int i = 0; i < 8; i++) {
            int offsetX = random.nextInt(size) - size/2;
            int offsetZ = random.nextInt(size) - size/2;
            int crystalHeight = 2 + random.nextInt(4);
            
            for (int dy = 0; dy < crystalHeight; dy++) {
                if (region.isInRegion(x + offsetX, y + dy, z + offsetZ)) {
                    if (dy == 0) {
                        region.setType(x + offsetX, y + dy, z + offsetZ, Material.BUDDING_AMETHYST);
                    } else {
                        region.setType(x + offsetX, y + dy, z + offsetZ, Material.AMETHYST_BLOCK);
                    }
                }
            }
        }
        
        // Add some floating crystals
        for (int i = 0; i < 5; i++) {
            int offsetX = random.nextInt(size + 4) - size/2 - 2;
            int offsetZ = random.nextInt(size + 4) - size/2 - 2;
            int offsetY = 2 + random.nextInt(6);
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.AMETHYST_BLOCK);
            }
        }
    }

    private void generateWarpedFortress(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate a ruined fortress
        int width = 10 + random.nextInt(10); // 10-19 blocks wide
        int length = 10 + random.nextInt(10); // 10-19 blocks long
        int height = 6 + random.nextInt(8); // 6-13 blocks tall
        
        // Fortress walls
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                for (int dy = 0; dy < height; dy++) {
                    int blockX = x + dx;
                    int blockY = y + dy;
                    int blockZ = z + dz;
                    
                    if (!region.isInRegion(blockX, blockY, blockZ)) continue;
                    
                    Material material;
                    if (dy == 0) {
                        material = Material.BLACKSTONE;
                    } else if (dy == height - 1) {
                        // Crenellations - mostly missing
                        if (random.nextDouble() > 0.5) {
                            material = Material.BLACKSTONE;
                        } else {
                            material = Material.AIR;
                        }
                    } else if (dx == 0 || dx == width - 1 || dz == 0 || dz == length - 1) {
                        // Walls - heavily damaged
                        if (random.nextDouble() > 0.3) {
                            material = Material.BLACKSTONE;
                        } else {
                            material = Material.AIR;
                        }
                    } else {
                        material = Material.AIR;
                    }
                    
                    region.setType(blockX, blockY, blockZ, material);
                }
            }
        }
        
        // Add some warped elements
        for (int i = 0; i < 10; i++) {
            int offsetX = random.nextInt(width);
            int offsetZ = random.nextInt(length);
            if (region.isInRegion(x + offsetX, y + 1, z + offsetZ)) {
                region.setType(x + offsetX, y + 1, z + offsetZ, Material.WARPED_WART_BLOCK);
            }
        }
        
        // Add some floating debris
        for (int i = 0; i < 12; i++) {
            int offsetX = random.nextInt(width + 6) - 3;
            int offsetZ = random.nextInt(length + 6) - 3;
            int offsetY = 1 + random.nextInt(4);
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.BLACKSTONE);
            }
        }
    }

    private void generateSculkMonument(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate a sculk-infested monument
        int size = 8 + random.nextInt(6); // 8-13 blocks
        
        // Base structure
        for (int dx = -size/2; dx <= size/2; dx++) {
            for (int dz = -size/2; dz <= size/2; dz++) {
                int blockX = x + dx;
                int blockZ = z + dz;
                
                if (!region.isInRegion(blockX, y, blockZ)) continue;
                
                if (random.nextDouble() > 0.6) {
                    region.setType(blockX, y, blockZ, Material.BLACKSTONE);
                }
            }
        }
        
        // Add sculk elements
        for (int i = 0; i < 15; i++) {
            int offsetX = random.nextInt(size) - size/2;
            int offsetZ = random.nextInt(size) - size/2;
            int offsetY = random.nextInt(3);
            
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                Material sculkMaterial;
                double rand = random.nextDouble();
                if (rand < 0.1) {
                    sculkMaterial = Material.SCULK_CATALYST;
                } else if (rand < 0.3) {
                    sculkMaterial = Material.SCULK_SENSOR;
                } else {
                    sculkMaterial = Material.SCULK;
                }
                region.setType(x + offsetX, y + offsetY, z + offsetZ, sculkMaterial);
            }
        }
        
        // Add some warped elements
        for (int i = 0; i < 8; i++) {
            int offsetX = random.nextInt(size) - size/2;
            int offsetZ = random.nextInt(size) - size/2;
            int offsetY = 1 + random.nextInt(2);
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.WARPED_WART_BLOCK);
            }
        }
    }

    private void generateFloatingRuins(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate floating ruin fragments
        int fragmentCount = 3 + random.nextInt(5); // 3-7 fragments
        
        for (int i = 0; i < fragmentCount; i++) {
            int offsetX = random.nextInt(20) - 10;
            int offsetZ = random.nextInt(20) - 10;
            int offsetY = 2 + random.nextInt(8);
            int fragmentSize = 2 + random.nextInt(4);
            
            // Create floating fragment
            for (int dx = 0; dx < fragmentSize; dx++) {
                for (int dz = 0; dz < fragmentSize; dz++) {
                    for (int dy = 0; dy < fragmentSize; dy++) {
                        int blockX = x + offsetX + dx;
                        int blockY = y + offsetY + dy;
                        int blockZ = z + offsetZ + dz;
                        
                        if (!region.isInRegion(blockX, blockY, blockZ)) continue;
                        
                        if (random.nextDouble() > 0.3) {
                            Material material = random.nextDouble() < 0.7 ? Material.BLACKSTONE : Material.WARPED_WART_BLOCK;
                            region.setType(blockX, blockY, blockZ, material);
                        }
                    }
                }
            }
        }
        
        // Add some connecting chains of blocks
        for (int i = 0; i < 5; i++) {
            int startX = x + random.nextInt(20) - 10;
            int startZ = z + random.nextInt(20) - 10;
            int startY = y + 2 + random.nextInt(6);
            
            for (int j = 0; j < 3; j++) {
                int blockX = startX + random.nextInt(3) - 1;
                int blockY = startY + j;
                int blockZ = startZ + random.nextInt(3) - 1;
                
                if (region.isInRegion(blockX, blockY, blockZ)) {
                    region.setType(blockX, blockY, blockZ, Material.BLACKSTONE);
                }
            }
        }
    }

    private void prepareRuinsArea(@NotNull LimitedRegion region, int centerX, int centerY, int centerZ, @NotNull WorldInfo worldInfo) {
        // Only clear a small area around the ruins (8x8 instead of 30x30)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;
                
                // Only clear vegetation in a small area
                for (int dy = centerY; dy <= centerY + 8; dy++) {
                    if (region.isInRegion(x, dy, z)) {
                        Material block = region.getType(x, dy, z);
                        if (block == Material.WARPED_STEM || block == Material.WARPED_WART_BLOCK || 
                            block == Material.WARPED_ROOTS || block == Material.WARPED_FUNGUS) {
                            region.setType(x, dy, z, Material.AIR);
                        }
                    }
                }
                
                // Ensure solid ground only in the immediate area
                if (region.isInRegion(x, centerY - 1, z)) {
                    region.setType(x, centerY - 1, z, Material.WARPED_NYLIUM);
                }
            }
        }
    }

    private int findSuitableRuinsLocation(@NotNull LimitedRegion region, int x, int z, @NotNull WorldInfo worldInfo) {
        // Look for interesting terrain features for ruins
        int bestY = -1;
        int bestScore = 0;
        
        for (int y = worldInfo.getMinHeight() + 10; y < worldInfo.getMaxHeight() - 20; y++) {
            if (!region.isInRegion(x, y, z)) continue;
            
            int score = evaluateLocation(region, x, y, z, worldInfo);
            if (score > bestScore) {
                bestScore = score;
                bestY = y;
            }
        }
        
        return bestY;
    }
    
    private int evaluateLocation(@NotNull LimitedRegion region, int x, int y, int z, @NotNull WorldInfo worldInfo) {
        int score = 0;
        
        // Check for solid ground
        if (!region.isInRegion(x, y - 1, z)) return 0;
        Material groundBlock = region.getType(x, y - 1, z);
        if (groundBlock == Material.AIR || groundBlock == Material.WATER || groundBlock == Material.LAVA) {
            return 0;
        }
        
        // Prefer locations with some height variation
        int heightVariation = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (region.isInRegion(x + dx, y, z + dz)) {
                    Material block = region.getType(x + dx, y, z + dz);
                    if (block != Material.AIR) {
                        heightVariation++;
                    }
                }
            }
        }
        
        // Score based on terrain features
        if (heightVariation > 10) score += 20; // Good height variation
        if (heightVariation < 5) score += 10;  // Some open space
        
        // Prefer locations near cliffs or hills
        boolean nearCliff = false;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (region.isInRegion(x + dx, y + 3, z + dz)) {
                    Material block = region.getType(x + dx, y + 3, z + dz);
                    if (block != Material.AIR) {
                        nearCliff = true;
                        break;
                    }
                }
            }
        }
        if (nearCliff) score += 15;
        
        // Prefer locations with some existing structures nearby
        boolean nearStructure = false;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (region.isInRegion(x + dx, y, z + dz)) {
                    Material block = region.getType(x + dx, y, z + dz);
                    if (block == Material.WARPED_STEM || block == Material.BLACKSTONE) {
                        nearStructure = true;
                        break;
                    }
                }
            }
        }
        if (nearStructure) score += 10;
        
        return score;
    }

    private void createFoundation(@NotNull LimitedRegion region, int x, int y, int z, int radius) {
        // Create a foundation that follows the terrain
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int blockX = x + dx;
                int blockZ = z + dz;
                
                if (!region.isInRegion(blockX, y - 1, blockZ)) continue;
                
                // Create foundation blocks
                region.setType(blockX, y - 1, blockZ, Material.BLACKSTONE);
                
                // Add some foundation depth
                if (region.isInRegion(blockX, y - 2, blockZ)) {
                    region.setType(blockX, y - 2, blockZ, Material.BLACKSTONE);
                }
            }
        }
    }

    private void generateUndergroundRuins(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate ruins partially buried underground
        int depth = 2 + random.nextInt(4); // 2-5 blocks deep
        int width = 4 + random.nextInt(4); // 4-7 blocks wide
        int length = 4 + random.nextInt(4); // 4-7 blocks long
        
        // Create underground chamber
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                for (int dy = 0; dy < depth; dy++) {
                    int blockX = x + dx;
                    int blockY = y - dy;
                    int blockZ = z + dz;
                    
                    if (!region.isInRegion(blockX, blockY, blockZ)) continue;
                    
                    if (dy == 0) {
                        // Floor
                        region.setType(blockX, blockY, blockZ, Material.BLACKSTONE);
                    } else if (dx == 0 || dx == width - 1 || dz == 0 || dz == length - 1) {
                        // Walls - partially collapsed
                        if (random.nextDouble() > 0.3) {
                            region.setType(blockX, blockY, blockZ, Material.BLACKSTONE);
                        }
                    } else {
                        // Interior - air
                        region.setType(blockX, blockY, blockZ, Material.AIR);
                    }
                }
            }
        }
        
        // Add some sculk elements
        for (int i = 0; i < 8; i++) {
            int offsetX = random.nextInt(width);
            int offsetZ = random.nextInt(length);
            if (region.isInRegion(x + offsetX, y - 1, z + offsetZ)) {
                region.setType(x + offsetX, y - 1, z + offsetZ, Material.SCULK);
            }
        }
        
        // Add some warped elements
        for (int i = 0; i < 5; i++) {
            int offsetX = random.nextInt(width);
            int offsetZ = random.nextInt(length);
            if (region.isInRegion(x + offsetX, y - 1, z + offsetZ)) {
                region.setType(x + offsetX, y - 1, z + offsetZ, Material.WARPED_WART_BLOCK);
            }
        }
    }

    private void generateCliffsideRuins(@NotNull LimitedRegion region, @NotNull Random random, int x, int y, int z) {
        // Generate ruins built into a cliff face
        int height = 4 + random.nextInt(6); // 4-9 blocks tall
        int width = 3 + random.nextInt(4); // 3-6 blocks wide
        int depth = 2 + random.nextInt(3); // 2-4 blocks deep
        
        // Create cliffside structure
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                for (int dy = 0; dy < height; dy++) {
                    int blockX = x + dx;
                    int blockY = y + dy;
                    int blockZ = z + dz;
                    
                    if (!region.isInRegion(blockX, blockY, blockZ)) continue;
                    
                    if (dy == 0) {
                        // Floor
                        region.setType(blockX, blockY, blockZ, Material.BLACKSTONE);
                    } else if (dx == 0 || dz == depth - 1) {
                        // Walls - heavily damaged
                        if (random.nextDouble() > 0.4) {
                            region.setType(blockX, blockY, blockZ, Material.BLACKSTONE);
                        }
                    } else {
                        // Interior - air
                        region.setType(blockX, blockY, blockZ, Material.AIR);
                    }
                }
            }
        }
        
        // Add some warped elements growing from the cliff
        for (int i = 0; i < 6; i++) {
            int offsetX = random.nextInt(width);
            int offsetZ = random.nextInt(depth);
            int offsetY = random.nextInt(height);
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.WARPED_ROOTS);
            }
        }
        
        // Add some floating debris
        for (int i = 0; i < 3; i++) {
            int offsetX = random.nextInt(8) - 4;
            int offsetZ = random.nextInt(8) - 4;
            int offsetY = random.nextInt(3) + 1;
            if (region.isInRegion(x + offsetX, y + offsetY, z + offsetZ)) {
                region.setType(x + offsetX, y + offsetY, z + offsetZ, Material.BLACKSTONE);
            }
        }
    }

    private int findSuitableGround(@NotNull LimitedRegion region, int x, int z, @NotNull WorldInfo worldInfo) {
        // Find solid ground, not water or air
        for (int y = worldInfo.getMaxHeight() - 1; y >= worldInfo.getMinHeight(); y--) {
            if (!region.isInRegion(x, y, z)) continue;
            
            Material block = region.getType(x, y, z);
            if (block != Material.AIR && block != Material.WATER && block != Material.LAVA) {
                // Check if there's enough space above for ruins
                boolean hasSpace = true;
                for (int checkY = y + 1; checkY <= y + 15; checkY++) {
                    if (region.isInRegion(x, checkY, z)) {
                        Material aboveBlock = region.getType(x, checkY, z);
                        if (aboveBlock != Material.AIR) {
                            hasSpace = false;
                            break;
                        }
                    }
                }
                if (hasSpace) {
                    return y + 1;
                }
            }
        }
        return -1;
    }
}
