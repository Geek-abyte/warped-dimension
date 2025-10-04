package me.swissh.warped_dimension;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class WarpedWorldListener implements Listener {
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Only apply to warped dimension
        if (!event.getWorld().getName().equals("warped_dimension")) {
            return;
        }
        
        // Remove any lava in loaded chunks
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = event.getWorld().getMinHeight(); y < event.getWorld().getMaxHeight(); y++) {
                    Block block = event.getChunk().getBlock(x, y, z);
                    if (block.getType() == Material.LAVA) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onLavaFlow(BlockFromToEvent event) {
        // Prevent lava from flowing in warped dimension
        if (!event.getBlock().getWorld().getName().equals("warped_dimension")) {
            return;
        }
        
        if (event.getBlock().getType() == Material.LAVA) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }
}

