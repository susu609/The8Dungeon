package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)       // Forge bus (mặc định), giống VoidSpawn
public final class DungeonAutoBuild {
    private DungeonAutoBuild() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;
        DungeonSavedData data = DungeonSavedData.get(level);
        if (!data.present()) {
            // World mới, chưa có dungeon: dựng ngay
            long seed = level.getSeed();                         // cùng seed world -> cùng dungeon
            DungeonLayout layout = DungeonService.build(level, seed);
            Vec3 p = DungeonService.startPos(layout);
            player.teleportTo(p.x, p.y, p.z);
            level.setDefaultSpawnPos(BlockPos.containing(p), 0F);   // chết thì hồi sinh trong phòng vào
        }
        // TODO 5 (bước 3): gửi seed cho NGƯỜI CHƠI NÀY


    }
}