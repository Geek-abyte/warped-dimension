package me.swissh.warped_dimension;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Warped_dimension extends JavaPlugin {

    private WarpedMobSpawner mobSpawner;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Warped Dimension plugin is starting...");

        // Register event listeners
        getServer().getPluginManager().registerEvents(new WarpedMobSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new WarpedPortalListener(this), this);
        getServer().getPluginManager().registerEvents(new WarpedWorldListener(), this);

        // Create or load the warped dimension world
        Bukkit.getScheduler().runTaskLater(this, () -> {
            World warpedWorld = WarpedWorldManager.getOrCreateWarpedWorld(this);
            if (warpedWorld != null) {
                getLogger().info("Warped Dimension is ready!");

                // Start passive mob spawner (runs every 5 seconds for more frequent spawns)
                mobSpawner = new WarpedMobSpawner(this);
                mobSpawner.runTaskTimer(this, 100L, 100L); // 5 second intervals
                getLogger().info("Warped Dimension mob spawner started!");
            }
        }, 20L); // Wait 1 second after server start

        getLogger().info("Warped Dimension plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (mobSpawner != null) {
            mobSpawner.cancel();
        }
        getLogger().info("Warped Dimension plugin disabled!");
    }
}
