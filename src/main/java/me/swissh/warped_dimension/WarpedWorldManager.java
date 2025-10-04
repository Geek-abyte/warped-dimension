package me.swissh.warped_dimension;

import org.bukkit.*;

public class WarpedWorldManager {

    public static World getOrCreateWarpedWorld(Warped_dimension plugin) {
        String worldName = "warped_dimension";
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            return world;
        }

        // Create world with custom generator
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new WarpedWorldGenerator());
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.AMPLIFIED);

        world = creator.createWorld();

        if (world != null) {
            // Set world properties
            world.setDifficulty(Difficulty.NORMAL);
            world.setSpawnFlags(true, true);
            world.setPVP(true);
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
            world.setAutoSave(true);

            plugin.getLogger().info("Warped Dimension world created successfully!");
        } else {
            plugin.getLogger().severe("Failed to create Warped Dimension world!");
        }

        return world;
    }
}

