/*
package net.ss.sudungeon;

import com.google.common.io.Files;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Mod.EventBusSubscriber(modid = SsMod.MODID)
public class WorldSaveCopier {

    @SubscribeEvent
public static void onWorldLoad(LevelEvent.Load event) {
    if (event.getLevel() instanceof ServerLevel level && level.getServer().isSingleplayer()) {
        // Only copy in singleplayer mode
        try {
            // Path to the sample world file in the resources folder
            Path source = Paths.get(
                    "src", "main", "resources", "assets", SsMod.MODID, "worlds", "my_dungeon_world.dat");

            // Path to the "saves" folder of Minecraft
            Path savesDir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("saves");

            // Create a new path for the new world file (using the new world name)
            Path destination = savesDir.resolve(level.getLevel().getLevelData().getName() + ".dat");

            // Copy the file
            java.nio.file.Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

            SsMod.LOGGER.info("Copied sample world file to: {}", destination);
        } catch (IOException e) {
            SsMod.LOGGER.error("Error copying world file: {}", e.getMessage());
        }
    }
}
}
*/
