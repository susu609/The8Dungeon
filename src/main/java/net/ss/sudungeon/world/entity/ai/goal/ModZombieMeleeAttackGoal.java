package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.ss.sudungeon.world.entity.AttackAction;
import net.ss.sudungeon.world.entity.ModZombieEntity;
import org.jetbrains.annotations.NotNull;

public class ModZombieMeleeAttackGoal extends ModMeleeAttackGoal {

    public ModZombieMeleeAttackGoal(ModZombieEntity mob, double speedModifier, boolean followEvenIfNotSeen) {
        super(mob, speedModifier, followEvenIfNotSeen, AttackAction.ZOMBIE_ATTACK);
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

        double distanceSq = modmob.distanceToSqr(target);
        double rangeSq = attackAction.range(modmob.getScale()) * attackAction.range(modmob.getScale());

        if (distanceSq <= rangeSq && isTargetInAttackArc()) {
            startAttack();
        } else {
            modmob.getNavigation().moveTo(target, speedModifier);
        }
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target, double squaredDistance) {
        // Không dùng
    }
}
