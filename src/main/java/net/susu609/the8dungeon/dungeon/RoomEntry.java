package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

/**
 * Người chơi bước sang ô mới → xử lý vào phòng NGAY trong tick đó.
 * Mỗi ô dungeon trùng đúng 1 chunk, nên "đổi chunk" = "đổi phòng"; Forge báo đúng một lần, không cần đếm tick.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class RoomEntry {

    private static final double PUSH = 1.5;   // đẩy khỏi lỗ cửa, để gate sập không kẹt người

    private RoomEntry() {
    }

    @SubscribeEvent
    public static void onEnterSection(EntityEvent.EnteringSection event) {
        if (!event.didChunkChange() || !(event.getEntity() instanceof ServerPlayer player)) return;
        DungeonView view = DungeonView.of(player.serverLevel());
        if (view == null) return;

        SectionPos to = event.getNewPos();
        DungeonLayout.Room here = view.roomAtChunk(to.x(), to.z());
        if (here == null) return;                                       // ngoài dungeon
        if (view.data().state(here.id()) != RoomState.HIDDEN) return;   // phòng đã biết: đi tự do

        // Đổi chunk xảy ra ngay ở lớp tường (local 0) = đang đứng trong lỗ cửa → đẩy vào trong.
        SectionPos from = event.getOldPos();
        player.teleportTo(
                player.getX() + (to.x() - from.x()) * PUSH,
                player.getY(),
                player.getZ() + (to.z() - from.z()) * PUSH);

        RoomCombat.enter(player, view, here);
    }
}
