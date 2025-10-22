package me.swissh.warped_dimension;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WarpedBiomeProvider extends BiomeProvider {

    private final Random random = new Random();

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        // Create biome distribution based on coordinates for consistent generation
        long seed = worldInfo.getSeed();
        random.setSeed(seed + x * 12345L + z * 67890L);
        
        // Use noise-based distribution for natural-looking biome boundaries
        double noise = getBiomeNoise(x, z, seed);
        
        // Biome distribution: 33% each biome for more balanced areas
        if (noise > 0.67) {
            return Biome.END_HIGHLANDS; // Amethyst Forest (33%)
        } else if (noise > 0.33) {
            return Biome.DEEP_DARK; // Sculk Forest (33%)
        } else {
            return Biome.WARPED_FOREST; // Regular Warped Forest (33%)
        }
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return Arrays.asList(Biome.WARPED_FOREST, Biome.DEEP_DARK, Biome.END_HIGHLANDS);
    }

    /**
     * Get a biome type based on coordinates for custom generation logic
     */
    public WarpedBiomes getWarpedBiomeType(int x, int z, long seed) {
        double noise = getBiomeNoise(x, z, seed);
        
        if (noise > 0.7) {
            return WarpedBiomes.AMETHYST_FOREST;
        } else if (noise > 0.4) {
            return WarpedBiomes.SCULK_FOREST;
        } else {
            return WarpedBiomes.WARPED_FOREST;
        }
    }

    /**
     * Get biome type from actual biome
     */
    public WarpedBiomes getWarpedBiomeType(Biome biome) {
        if (biome == Biome.END_HIGHLANDS) {
            return WarpedBiomes.AMETHYST_FOREST;
        } else if (biome == Biome.DEEP_DARK) {
            return WarpedBiomes.SCULK_FOREST;
        } else {
            return WarpedBiomes.WARPED_FOREST;
        }
    }

    /**
     * Improved noise function for biome distribution - creates larger, more coherent areas
     */
    private double getBiomeNoise(int x, int z, long seed) {
        // Use much lower frequency noise for larger biome areas
        // Scale down coordinates to create larger regions (divide by larger numbers)
        double scale1 = 0.001; // Very large areas
        double scale2 = 0.002; // Medium-large areas
        double scale3 = 0.003; // Medium areas
        
        // Add some randomness based on seed for variation
        Random random = new Random(seed + x * 12345L + z * 67890L);
        double randomOffset = (random.nextDouble() - 0.5) * 0.1;
        
        // Create layered noise for more natural boundaries
        double noise1 = Math.sin(x * scale1) * Math.cos(z * scale1);
        double noise2 = Math.sin(x * scale2 + z * scale2) * 0.6;
        double noise3 = Math.sin(x * scale3 - z * scale3) * 0.4;
        
        // Combine and normalize to 0-1 range
        double combined = (noise1 + noise2 + noise3 + randomOffset + 2.0) / 4.0;
        return Math.max(0.0, Math.min(1.0, combined)); // Clamp to 0-1
    }
}
