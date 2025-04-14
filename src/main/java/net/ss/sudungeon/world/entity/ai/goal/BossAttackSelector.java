package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.util.RandomSource;
import net.ss.sudungeon.world.entity.ZombieBossEntity;
import java.util.Random;

public class BossAttackSelector {
    public static void selectPhase1Attack(ZombieBossEntity boss) {
        boss.setAttacking(true);
        boss.level().broadcastEntityEvent(boss, (byte) 4); // ZOMBIE_ATTACK
    }

    public static void selectPhase2Attack(ZombieBossEntity boss) {
        RandomSource random = boss.level().getRandom(); // ✅ dùng đúng kiểu
        int choice = random.nextInt(3);

        switch (choice) {
            case 0 -> {
                boss.setAttacking(true);
                boss.level().broadcastEntityEvent(boss, (byte) 4); // ZOMBIE_ATTACK
            }
            case 1 -> {
                boss.setPerformingSlam(true);
                boss.level().broadcastEntityEvent(boss, (byte) 5); // ZOMBIE_GROUND_SLAM
            }
            case 2 -> {
                boss.setPerformingWideSlash(true);
                boss.level().broadcastEntityEvent(boss, (byte) 6); // ZOMBIE_WIDE_SLASH
            }
        }
    }
}
