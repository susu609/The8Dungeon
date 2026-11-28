package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Người chơi bước sang ô mới → xử lý vào phòng NGAY trong tick đó.
 * Mỗi ô dungeon trùng đúng 1 chunk, nên "đổi chunk" = "đổi phòng"; Forge báo đúng một lần, không cần đếm tick.
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class RoomEntry {

    private static final double PUSH = 1.5;   // đẩy khỏi lỗ cửa, để gate sập không kẹt người

    private RoomEntry() {
    }

    private static final Map<UUID, Pending> pending = new HashMap<>();

    @SubscribeEvent
    public static void onEnterSection(EntityEvent.EnteringSection event) {
        if (!event.didChunkChange() || !(event.getEntity() instanceof ServerPlayer player)) return;
        DungeonView view = DungeonView.of(player.serverLevel());
        if (view == null) return;

        SectionPos to = event.getNewPos();
        DungeonLayout.Room here = view.roomAtChunk(to.x(), to.z());
        if (here == null || view.data().state(here.id()) != RoomState.HIDDEN) {
            pending.remove(player.getUUID());                 // quay ra / sang phòng đã biết: huỷ
            return;
        }
        pending.put(player.getUUID(), new Pending(here.id(), player.tickCount));   // CHƯA vào combat
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Pending p = pending.get(player.getUUID());
        if (p == null) return;
        if (player.tickCount - p.sinceTick() > 100) {
            pending.remove(player.getUUID());
            return;
        }   // 5 giây không vào hẳn

        DungeonView view = DungeonView.of(player.serverLevel());
        DungeonLayout.Room room = view == null ? null : view.room(p.roomId());
        if (room == null) {
            pending.remove(player.getUUID());
            return;
        }

        AABB core = DungeonBuilder.roomBox(view.origin(), room).deflate(2, 0, 2);   // cách tường ≥ 2 block
        if (!core.contains(player.position())) return;                              // chưa vào hẳn

        pending.remove(player.getUUID());
        if (view.data().state(room.id()) == RoomState.HIDDEN) RoomCombat.enter(player, view, room);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        pending.remove(event.getEntity().getUUID());
    }

    private record Pending(int roomId, int sinceTick) {
    }
}
