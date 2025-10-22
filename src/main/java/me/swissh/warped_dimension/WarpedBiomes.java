package me.swissh.warped_dimension;

import org.bukkit.block.Biome;

/**
 * Custom biome definitions for the Warped Dimension
 */
public enum WarpedBiomes {
    WARPED_FOREST(Biome.WARPED_FOREST, "Warped Forest"),
    SCULK_FOREST(Biome.DEEP_DARK, "Sculk Forest"),
    AMETHYST_FOREST(Biome.END_HIGHLANDS, "Amethyst Forest");

    private final Biome baseBiome;
    private final String displayName;

    WarpedBiomes(Biome baseBiome, String displayName) {
        this.baseBiome = baseBiome;
        this.displayName = displayName;
    }

    public Biome getBaseBiome() {
        return baseBiome;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if a biome is a sculk forest biome
     */
    public boolean isSculkForest() {
        return this == SCULK_FOREST;
    }

    /**
     * Check if a biome is a warped forest biome
     */
    public boolean isWarpedForest() {
        return this == WARPED_FOREST;
    }

    /**
     * Check if a biome is an amethyst forest biome
     */
    public boolean isAmethystForest() {
        return this == AMETHYST_FOREST;
    }
}
