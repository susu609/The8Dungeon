/*
package net.ss.sudungeon.world;

import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class WorldManager {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCreateSpawnPosition(LevelEvent.CreateSpawnPosition event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Lấy đường dẫn lưu trữ thế giới
        Path worldPath = serverLevel.getServer().getWorldPath(LevelResource.ROOT);
        File worldFolder = worldPath.toFile();

        // Đường dẫn đến thư mục tài nguyên mặc định
        File defaultWorldFolder = new File("src/main/resources/default_world/Save Game");

        try {
            // Sao chép thư mục mặc định vào thư mục thế giới mới
            FileUtils.copyDirectory(defaultWorldFolder, worldFolder);
            LOGGER.info("Default world copied successfully!");
        } catch (IOException e) {
            LOGGER.error("Error copying default world: ", e);
        }
    }
}
*/
