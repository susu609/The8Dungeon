package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.ss.sudungeon.world.entity.ZombieBossEntity;
import net.ss.sudungeon.world.entity.AttackAction;

public class ZombieGroundSlamAttackGoal extends ModMeleeAttackGoal {

    public ZombieGroundSlamAttackGoal(ZombieBossEntity mob) {
        super(mob, 0.8, true, AttackAction.ZOMBIE_GROUND_SLAM);
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
                performAttack(target);
                attackCooldown = attackAction.cooldownTicks();
                attackChargeTick = 0;
            }
            return;
        }

        double distanceSq = modmob.distanceToSqr(target);
        double rangeSq = attackAction.range(modmob.getBbWidth()) * attackAction.range(modmob.getBbWidth());

        if (distanceSq <= rangeSq) {
            startAttack();
        } else {
            modmob.getNavigation().moveTo(target, speedModifier);
        }
    }

    protected void performAttack(LivingEntity target) {
        AABB area = modmob.getBoundingBox().inflate(attackAction.range(modmob.getBbWidth()));
        for (LivingEntity e : modmob.level().getEntitiesOfClass(LivingEntity.class, area, EntitySelector.NO_SPECTATORS)) {
            if (e != modmob) {
                e.hurt(modmob.damageSources().mobAttack(modmob), attackAction.damage(modmob.getBbWidth()));
            }
        }
    }
}
