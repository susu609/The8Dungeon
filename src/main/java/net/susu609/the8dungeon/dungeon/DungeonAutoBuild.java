package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

/**
 * Dựng dungeon một lần khi server khởi động xong (world mới).
 * Lúc này vùng spawn đã nạp sẵn, chưa có người chơi, nên ánh sáng kịp tính trước khi ai đó vào.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class DungeonAutoBuild {

    private DungeonAutoBuild() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        DungeonSavedData data = DungeonSavedData.get(level);
        if (data.present()) return;                       // world cũ: đã có dungeon
        try {
            DungeonLayout layout = DungeonService.build(level, level.getSeed());
            level.setDefaultSpawnPos(BlockPos.containing(DungeonService.startPos(layout)), 0F);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Susu609Mod.LOGGER.error("Không dựng được dungeon", e);
        }
    }
}
