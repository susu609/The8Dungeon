package net.susu609.the8dungeon.dungeon;

import net.susu609.the8dungeon.dungeon.RoomMob;

import java.util.ArrayList;
import java.util.List;

import static net.susu609.the8dungeon.dungeon.RoomMob.*;

public enum MobPool {
    // tier 0: gần cửa vào, đơn lẻ
    ONE_ZOMBIE(0, ZOMBIE),
    TWO_ZOMBIES(0, ZOMBIE, ZOMBIE),
    ONE_SKELETON(0, SKELETON),
    // tier 1: bắt đầu phối hợp
    THREE_ZOMBIES(1, ZOMBIE, ZOMBIE, ZOMBIE),
    TWO_ZOMBIES_ONE_SKELETON(1, ZOMBIE, ZOMBIE, SKELETON),
    TWO_SKELETONS(1, SKELETON, SKELETON),
    // tier 2: sâu, đông
    FOUR_ZOMBIES(2, ZOMBIE, ZOMBIE, ZOMBIE, ZOMBIE),
    THREE_ZOMBIES_ONE_SKELETON(2, ZOMBIE, ZOMBIE, ZOMBIE, SKELETON),
    TWO_ZOMBIES_TWO_SKELETONS(2, ZOMBIE, ZOMBIE, SKELETON, SKELETON);

    public final int tier;
    public final RoomMob[] mobs;

    MobPool(int tier, RoomMob... mobs) { this.tier = tier; this.mobs = mobs; }

    public static List<MobPool> ofTier(int tier) {
        List<MobPool> out = new ArrayList<>();
        for (MobPool p : values()) if (p.tier == tier) out.add(p);
        return out;
    }
}