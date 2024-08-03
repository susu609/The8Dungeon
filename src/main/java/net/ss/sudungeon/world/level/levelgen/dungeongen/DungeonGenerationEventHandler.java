/*
package net.ss.sudungeon.world.level.levelgen.dungeongen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.ss.sudungeon.DungeonSavedData;
import net.ss.sudungeon.SsMod;

// ... (các import khác)

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonGenerationEventHandler {
    private static final int TICKS_PER_STEP = 20; // 20 ticks = 1 giây
    private static final int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick (TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (ServerLevel world : event.getServer().getAllLevels()) {
                DungeonSavedData data = DungeonSavedData.get(world);
                if (data.getDungeonGenerator() != null) {
                    SsMod.LOGGER.info("Đang tạo dungeon...");
                    data.getDungeonGenerator().generate(world, new BlockPos(0, 1, 0), data.getDungeonSeed());
                    data.setGenerating(false);
                    data.setDirty();
                } else if (data.getRoomDataList().isEmpty()) {
                    SsMod.LOGGER.warn("Không tìm thấy dungeon generator cho thế giới {}", world.dimension().location());
                }
            }
        }
    }
}
*/
