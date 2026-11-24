package net.susu609.the8dungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;

import java.util.List;

public final class RoomCombat {
    public static final String SCANNING = "scanning";

    public static void start(ServerLevel level, BlockPos origin, DungeonLayout.Room room, ServerPlayer player) {
        AABB box = DungeonBuilder.roomBox(origin, room);
        for (Mob mob : mobsOf(level, box, room)) {
            mob.setNoAi(false);
            mob.setInvulnerable(false);
            mob.setTarget(player);
        }
        gates(level, box).forEach(g -> g.setLocked(true));
    }

    public static boolean cleared(ServerLevel level, BlockPos origin, DungeonLayout.Room room) {
        return mobsOf(level, DungeonBuilder.roomBox(origin, room), room).isEmpty();
    }

    public static void finish(ServerLevel level, BlockPos origin, DungeonLayout.Room room) {
        gates(level, DungeonBuilder.roomBox(origin, room)).forEach(g -> {
            g.setLocked(false);
            g.setOpen(true);
        });
    }

    private static List<Mob> mobsOf(ServerLevel level, AABB box, DungeonLayout.Room room) {
        String tag = DungeonBuilder.ROOM_TAG + room.id();
        return level.getEntitiesOfClass(Mob.class, box, m -> m.isAlive() && m.getTags().contains(tag));
    }

    private static List<GateEntity> gates(ServerLevel level, AABB box) {
        return level.getEntitiesOfClass(GateEntity.class, box.inflate(1));   // gate nằm trên mép
    }

}