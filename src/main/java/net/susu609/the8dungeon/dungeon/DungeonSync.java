package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.network.ClientboundDungeonSeedPacket;
import net.susu609.the8dungeon.network.ClientboundVisitedRoomsPacket;

/**
 * Đồng bộ dungeon xuống client: seed (để client tự dựng layout) và danh sách phòng đã biết.
 * Lần đầu vào world thì đưa người chơi vào phòng vào.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class DungeonSync {

    private DungeonSync() {
    }

    /** Gửi danh sách phòng đã biết cho MỌI người (dữ liệu chung của cả nhóm). */
    public static void sendRooms(DungeonSavedData data) {
        Susu609Mod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(),
                new ClientboundVisitedRoomsPacket(data.knownRooms()));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DungeonView view = DungeonView.of(player.serverLevel());
        if (view == null) return;                          // chưa có dungeon: không dịch, không gửi gì

        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean("entered_dungeon")) {
            BlockPos s = player.serverLevel().getSharedSpawnPos();
            player.teleportTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5);
            tag.putBoolean("entered_dungeon", true);
        }
        PacketDistributor.PacketTarget me = PacketDistributor.PLAYER.with(() -> player);
        Susu609Mod.PACKET_HANDLER.send(me, new ClientboundDungeonSeedPacket(true, view.data().seed()));
        // Gửi SAU seed: client cần layout trước mới vẽ được phòng.
        Susu609Mod.PACKET_HANDLER.send(me, new ClientboundVisitedRoomsPacket(view.data().knownRooms()));
    }
}
