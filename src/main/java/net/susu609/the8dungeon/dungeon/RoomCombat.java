package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;

import java.util.List;

/**
 * Luật trạng thái phòng: HIDDEN → (COMBAT) → CLEARED.
 * Không tự nghe event; RoomEntry và RoomClear gọi vào đây.
 */
public final class RoomCombat {

    private RoomCombat() {
    }

    /** Vừa bước vào phòng HIDDEN: có quái → COMBAT (khoá cửa, quái lao tới), không → CLEARED. */
    public static void enter(ServerPlayer player, DungeonView view, DungeonLayout.Room room) {
        ServerLevel level = player.serverLevel();
        AABB box = DungeonBuilder.roomBox(view.origin(), room);
        List<Mob> mobs = mobsOf(level, box, room);
        if (mobs.isEmpty()) {
            view.data().setState(room.id(), RoomState.CLEARED);
        } else {
            view.data().setState(room.id(), RoomState.COMBAT);
            for (Mob mob : mobs) {
                mob.setNoAi(false);
                mob.setInvulnerable(false);
                mob.setTarget(player);
                mob.lookAt(player, 360F, 360F);                 // quay mặt ngay
                mob.getNavigation().moveTo(player, 1.3);        // chạy luôn trong tick này
            }
            gates(level, box).forEach(g -> g.setLocked(true));
        }
        DungeonSync.sendRooms(view.data());
    }

    /** Hết quái: CLEARED, mở khoá và mở cửa. */
    public static void clear(ServerLevel level, DungeonView view, DungeonLayout.Room room) {
        view.data().setState(room.id(), RoomState.CLEARED);
        gates(level, DungeonBuilder.roomBox(view.origin(), room)).forEach(g -> {
            g.setLocked(false);
            g.setOpen(true);
        });
        DungeonSync.sendRooms(view.data());
    }

    /** Số quái còn sống của phòng, không tính 'except' (con đang chết). */
    public static int aliveExcept(ServerLevel level, DungeonView view, DungeonLayout.Room room, Mob except) {
        AABB box = DungeonBuilder.roomBox(view.origin(), room);
        return (int) mobsOf(level, box, room).stream().filter(m -> m != except).count();
    }

    private static List<Mob> mobsOf(ServerLevel level, AABB box, DungeonLayout.Room room) {
        String tag = DungeonBuilder.ROOM_TAG + room.id();
        return level.getEntitiesOfClass(Mob.class, box, m -> m.isAlive() && m.getTags().contains(tag));
    }

    private static List<GateEntity> gates(ServerLevel level, AABB box) {
        return level.getEntitiesOfClass(GateEntity.class, box.inflate(1));   // gate nằm trên mép phòng
    }
}
