package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.ss.sudungeon.world.entity.ModZombieEntity;
import net.ss.sudungeon.world.entity.ZombieBossEntity;
import net.ss.sudungeon.world.entity.AttackAction;

public class ZombieBossMeleeAttackGoal extends ModMeleeAttackGoal {

    public ZombieBossMeleeAttackGoal(ZombieBossEntity mob) {
        super(mob, 1.0, true, AttackAction.ZOMBIE_ATTACK);
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
                applyAttackEffects(modmob.getScale());
                attackCooldown = attackAction.cooldownTicks();
                attackChargeTick = 0;
            }
            return;
        }

        double distanceSq = modmob.distanceToSqr(target);
        double rangeSq = attackAction.range(modmob.getScale()) * attackAction.range(modmob.getScale());

        if (distanceSq <= rangeSq && isTargetInAttackArc()) {
            startAttack();
        } else {
            modmob.getNavigation().moveTo(target, speedModifier);
        }
    }
}
