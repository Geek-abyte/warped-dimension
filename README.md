# Warped Dimension Plugin

A Minecraft plugin that adds a custom dimension called the Warped Dimension - an amplified world with unique terrain, mobs, and aesthetics.

## Features

### World Generation
- **Amplified terrain** with the same settings as the overworld
- **Block replacements:**
  - Stone/Deepslate/Granite/Diorite/Andesite/Tuff → Amethyst Block
  - Dirt/Coarse Dirt/Podzol → Sculk
  - Grass Block → Warped Nylium
  - Short Grass/Tall Grass/Flowers → Warped Roots
  - Tree Logs → Warped Stem
  - Tree Leaves → Warped Wart Block
- **No lava generation** in the dimension
- **Warped trees** naturally generate throughout the world
- **Cyan sky** (achieved through ICE_SPIKES biome)

### Mob Spawning
Only specific mobs can spawn in the Warped Dimension:
- **Cows** (cold variants only - due to ice biome)
- **Chickens** (cold variants only)
- **Endermen**
- **Warden** (0.02% spawn chance - extremely rare!)

All other mobs are prevented from spawning.

### Portal System
- Create a portal frame using **Reinforced Deepslate**
- Portal frame follows the same size requirements as Nether portals:
  - Minimum size: 4x5 blocks (interior)
  - Maximum size: 23x23 blocks
- Ignite the portal with **Flint and Steel** or **Fire Charge**
- Step through to teleport to the Warped Dimension
- Use the same portal in the Warped Dimension to return to the Overworld

## Installation

1. Build the plugin using Maven:
   ```bash
   mvn clean package
   ```

2. Copy the generated JAR file from `target/warped_dimension-1.0-SNAPSHOT.jar` to your server's `plugins` folder

3. Restart your server

4. The Warped Dimension world will be automatically created on server startup

## Usage

### Creating a Portal

1. Build a rectangular frame out of Reinforced Deepslate blocks (minimum 4x5, maximum 23x23)
2. Right-click the frame with Flint and Steel or a Fire Charge
3. The portal will activate with purple portal blocks
4. Walk through the portal to enter the Warped Dimension

### Exploring the Warped Dimension

- Explore the amplified terrain made of amethyst, sculk, and warped nylium
- Watch out for rare Warden spawns!
- Collect unique resources
- Enjoy the cyan sky and warped forest aesthetic

## Technical Details

- **Minecraft Version:** 1.21+
- **API:** Spigot/Paper API
- **Java Version:** 21
- **World Name:** `warped_dimension`

## Files Created

The plugin consists of the following classes:

- `Warped_dimension.java` - Main plugin class
- `WarpedWorldGenerator.java` - Custom world generator for block replacements
- `WarpedBiomeProvider.java` - Provides the ICE_SPIKES biome for cyan sky and cold mobs
- `WarpedTreePopulator.java` - Generates warped trees throughout the world
- `WarpedMobSpawnListener.java` - Controls which mobs can spawn
- `WarpedPortalListener.java` - Handles portal creation and teleportation
- `WarpedWorldManager.java` - Manages world creation and settings

## Notes

- The Warped Dimension uses amplified terrain generation, which creates dramatic landscapes
- Cold variant cows and chickens spawn naturally due to the ice biome
- The Warden has only a 0.02% chance to spawn, making it extremely rare
- Portal linking between dimensions is automatic
- The world is automatically saved and persists between server restarts

## Support

If you encounter any issues, check your server logs for error messages. The plugin will log important events during startup and operation.

