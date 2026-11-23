package net.susu609.the8dungeon.dungeon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public enum RoomMob {
    ZOMBIE(EntityType.ZOMBIE, 1),
    SPIDER(EntityType.SPIDER, 1),
    SKELETON(EntityType.SKELETON, 2);

    public final EntityType<? extends Mob> type;
    public final int cost;                       // "điểm" nặng nhẹ

    RoomMob(EntityType<? extends Mob> type, int cost) {
        this.type = type;
        this.cost = cost;
    }
}