package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.ss.sudungeon.world.entity.ZombieBossEntity;
import java.util.EnumSet;

public class ZombieBossCombatGoal extends Goal {
    private final ZombieBossEntity boss;
    private int cooldown = 0;

    public ZombieBossCombatGoal(ZombieBossEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.boss.getTarget() != null && this.boss.getSensing().hasLineOfSight(this.boss.getTarget());

    }

    @Override
    public void tick() {
        if (--cooldown > 0) return;

        if (boss.getHealth() <= boss.getMaxHealth() * 0.5) {
            BossAttackSelector.selectPhase2Attack(boss);
        } else {
            BossAttackSelector.selectPhase1Attack(boss);
        }

        cooldown = 40 + boss.getRandom().nextInt(20); // cooldown ngẫu nhiên giữa lần chọn đòn
    }
}
