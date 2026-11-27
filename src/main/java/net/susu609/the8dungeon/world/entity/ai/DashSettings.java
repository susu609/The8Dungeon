package net.susu609.the8dungeon.world.entity.ai;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/** Số liệu chiêu dash; mỗi quái một bộ. Đơn vị thời gian: tick (20 tick = 1 giây). */
public record DashSettings(int aim, int lock, int dash, int recover, int cooldown,
                           double speed, float damage, double minDist, double maxDist,
                           SoundEvent sound) {

    public static final DashSettings KNIGHT =
            new DashSettings(12, 6, 8, 20, 100, 1.4, 8F, 4, 12, SoundEvents.RAVAGER_ROAR);
}
