package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WarpedWorldGenerator extends ChunkGenerator {

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Use amplified terrain generation by delegating to default generator
        // We'll modify blocks in generateSurface and generateBedrock
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Replace blocks after surface generation
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++) {
                    Material block = chunkData.getType(x, y, z);

                    // Replace stone with amethyst block
                    if (block == Material.STONE || block == Material.DEEPSLATE ||
                        block == Material.GRANITE || block == Material.DIORITE ||
                        block == Material.ANDESITE || block == Material.TUFF) {
                        chunkData.setBlock(x, y, z, Material.AMETHYST_BLOCK);
                    }
                    // Replace dirt with sculk
                    else if (block == Material.DIRT || block == Material.COARSE_DIRT ||
                             block == Material.ROOTED_DIRT || block == Material.PODZOL) {
                        chunkData.setBlock(x, y, z, Material.SCULK);
                    }
                    // Replace grass block with warped nylium
                    else if (block == Material.GRASS_BLOCK) {
                        chunkData.setBlock(x, y, z, Material.WARPED_NYLIUM);
                    }
                    // Replace tall grass, ferns, etc. with warped roots
                    else if (block == Material.SHORT_GRASS || block == Material.TALL_GRASS ||
                             block == Material.FERN || block == Material.LARGE_FERN ||
                             block == Material.DEAD_BUSH || block == Material.DANDELION ||
                             block == Material.POPPY || block == Material.BLUE_ORCHID ||
                             block == Material.ALLIUM || block == Material.AZURE_BLUET ||
                             block == Material.RED_TULIP || block == Material.ORANGE_TULIP ||
                             block == Material.WHITE_TULIP || block == Material.PINK_TULIP ||
                             block == Material.OXEYE_DAISY || block == Material.CORNFLOWER ||
                             block == Material.LILY_OF_THE_VALLEY || block == Material.SUNFLOWER ||
                             block == Material.LILAC || block == Material.ROSE_BUSH ||
                             block == Material.PEONY) {
                        chunkData.setBlock(x, y, z, Material.WARPED_ROOTS);
                    }
                    // Remove lava
                    else if (block == Material.LAVA) {
                        chunkData.setBlock(x, y, z, Material.AIR);
                    }
                    // Replace tree leaves with warped wart blocks
                    else if (block.name().contains("LEAVES")) {
                        chunkData.setBlock(x, y, z, Material.WARPED_WART_BLOCK);
                    }
                    // Replace tree logs with warped stems
                    else if (block.name().contains("LOG") || block.name().contains("WOOD")) {
                        chunkData.setBlock(x, y, z, Material.WARPED_STEM);
                    }
                }
            }
        }
    }

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Generate bedrock at the bottom
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, worldInfo.getMinHeight(), z, Material.BEDROCK);
                for (int y = worldInfo.getMinHeight() + 1; y < worldInfo.getMinHeight() + 5; y++) {
                    if (random.nextInt(y - worldInfo.getMinHeight()) == 0) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return true;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new WarpedBiomeProvider();
    }

    @NotNull
    @Override
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return Collections.singletonList(new WarpedTreePopulator());
    }
}
