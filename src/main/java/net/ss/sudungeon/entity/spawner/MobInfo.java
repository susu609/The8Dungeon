package net.ss.sudungeon.entity.spawner;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;

public record MobInfo(EntityType<? extends Monster> type, int challengeCost, float spawnProbability, int level) {
    public static final List<MobInfo> MOB_TYPES = List.of(
            new MobInfo(EntityType.ZOMBIE, 15, 0.7f, 1),     // Normal
            new MobInfo(EntityType.SKELETON, 20, 0.5f, 1),    // Normal
            new MobInfo(EntityType.SPIDER, 20, 0.3f, 1),      // Normal
            new MobInfo(EntityType.CREEPER, 30, 0.2f, 1)     // Normal
/*            new MobInfo(EntityType.HUSK, 25, 0.4f, 2),       // Elite
            new MobInfo(EntityType.STRAY, 35, 0.3f, 2),       // Elite
            new MobInfo(EntityType.WITCH, 30, 0.2f, 2),       // Elite
            new MobInfo(EntityType.ENDERMAN, 40, 0.1f, 2),     // Elite
            new MobInfo(EntityType.WITHER, 50, 0.05f, 3)       // Boss*/

            // ... thêm các loại quái vật khác
    );

    public boolean isValid () {
        return challengeCost >= 1 && challengeCost <= 100 && spawnProbability >= 0 && spawnProbability <= 1.0f;
    }

}
