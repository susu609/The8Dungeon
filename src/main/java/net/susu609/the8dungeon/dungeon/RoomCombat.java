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
            mob.setInvulnerable(false);      // đánh được rồi, nhưng chưa tỉnh
            mob.addTag(SCANNING);            // đang dò
        }
        gates(level, box).forEach(g -> g.setLocked(true));
    }

    public static void scan(ServerLevel level, AABB box, DungeonLayout.Room room, ServerPlayer player) {
        for (Mob mob : mobsOf(level, box, room)) {
            if (!mob.getTags().contains(SCANNING)) continue;

            boolean hurt = mob.getLastHurtByMob() != null;                  // bị đánh lén → tỉnh luôn
            if (hurt || sees(mob, player)) {
                mob.removeTag(SCANNING);
                mob.setNoAi(false);
                mob.setTarget(player);
                continue;
            }
            float yaw = mob.getYRot() + 6F;                                  // xoay chậm
            mob.setYRot(yaw);
            mob.setYHeadRot(yaw);
        }
    }

    private static boolean sees(Mob mob, ServerPlayer player) {
        Vec3 look = mob.getViewVector(1F);
        Vec3 to = player.getEyePosition().subtract(mob.getEyePosition()).normalize();
        boolean inCone = look.dot(to) > Math.cos(Math.toRadians(30));        // nửa nón 30° = nón 60°
        return inCone && mob.hasLineOfSight(player);
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