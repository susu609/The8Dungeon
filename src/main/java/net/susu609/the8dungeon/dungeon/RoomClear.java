package net.susu609.the8dungeon.dungeon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.susu609.the8dungeon.Susu609Mod;

/**
 * Quái của phòng chết → kiểm tra phòng hết quái chưa, NGAY lúc đó (không cần đếm tick).
 */
@Mod.EventBusSubscriber(modid = Susu609Mod.MODID)
public final class RoomClear {

    private RoomClear() {
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        Integer id = roomIdOf(mob);
        if (id == null) return;                                         // quái thường, không thuộc phòng

        DungeonView view = DungeonView.of(level);
        if (view == null || view.data().state(id) != RoomState.COMBAT) return;
        DungeonLayout.Room room = view.room(id);
        if (room == null) return;

        // Lúc event chạy, con vừa chết vẫn isAlive() → loại nó ra khi đếm.
        if (RoomCombat.aliveExcept(level, view, room, mob) > 0) return;
        RoomCombat.clear(level, view, room);
    }

    /** "room_12" → 12; null nếu quái không có tag phòng. */
    private static Integer roomIdOf(Mob mob) {
        for (String tag : mob.getTags()) {
            if (tag.startsWith(DungeonBuilder.ROOM_TAG)) {
                return Integer.parseInt(tag.substring(DungeonBuilder.ROOM_TAG.length()));
            }
        }
        return null;
    }
}
