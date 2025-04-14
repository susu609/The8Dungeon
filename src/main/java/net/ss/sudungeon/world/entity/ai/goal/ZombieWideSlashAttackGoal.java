package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.ss.sudungeon.world.entity.AttackAction;
import net.ss.sudungeon.world.entity.ZombieBossEntity;

public class ZombieWideSlashAttackGoal extends ModMeleeAttackGoal {
    private final float scale;

    public ZombieWideSlashAttackGoal(ZombieBossEntity mob) {
        super(mob, 0.9, true, AttackAction.ZOMBIE_WIDE_SLASH);
        this.scale = mob.getScale();
    }

    @Override
    protected void handleAttackLogic() {
        LivingEntity target = modmob.getTarget();
        if (target == null || !target.isAlive()) {
            resetAttackState();
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (attackChargeTick > 0) {
            attackChargeTick++;
            if (attackChargeTick == attackAction.delayTicks()) {
                applyAttackEffects(scale);
                attackCooldown = attackAction.cooldownTicks();
                attackChargeTick = 0;
            }
            return;
        }

        double distanceSq = modmob.distanceToSqr(target);
        double rangeSq = attackAction.range(scale) * attackAction.range(scale);

        if (distanceSq <= rangeSq && isTargetInAttackArc()) {
            startAttack();
        } else {
            modmob.getNavigation().moveTo(target, speedModifier);
        }
    }
}
