package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ss.sudungeon.init.SsModSounds;
import net.ss.sudungeon.world.entity.AttackAction;
import net.ss.sudungeon.world.entity.ModMonster;

import java.util.List;
import java.util.Objects;

public abstract class ModMeleeAttackGoal extends MeleeAttackGoal {
    protected final ModMonster modmob;
    protected AttackAction attackAction;
    protected int attackCooldown = 0;
    protected int attackChargeTick = 0;
    protected double originalSpeed;
    protected final double speedModifier;

    public ModMeleeAttackGoal (ModMonster modmob, double speedModifier, boolean followEvenIfNotSeen, AttackAction attackAction) {
        super(modmob, speedModifier, followEvenIfNotSeen);
        this.modmob = modmob;
        this.attackAction = attackAction;
        this.speedModifier = speedModifier;
    }

    @Override
    public void tick() {
        super.tick();

        // Nếu đang charge đòn đánh
        if (attackChargeTick > 0) {
            attackChargeTick++;

            // Gây sát thương đúng thời điểm
            if (attackChargeTick == attackAction.damageTick()) {
                applyAttackEffects(modmob.getScale());
            }

            // Reset khi animation kết thúc
            if (attackChargeTick >= attackAction.totalTicks()) {
                resetAttackState();
                attackCooldown = attackAction.cooldownTicks();
            }

            return;
        }

        // Nếu không charge thì gọi logic tấn công
        handleAttackLogic();
    }


    protected abstract void handleAttackLogic ();

    protected void startAttack () {
        if (!modmob.isAttacking()) {
            modmob.setAttacking(true);
            modmob.level().broadcastEntityEvent(modmob, (byte) attackAction.id);
        }
        attackChargeTick = 1;
        originalSpeed = modmob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        Objects.requireNonNull(modmob.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.0);
        modmob.playSound(SsModSounds.ZOMBIE_ATTACK.get(), 1.0F, modmob.getVoicePitch());
    }

    protected void applyAttackEffects (float scale) {
        modmob.setAttacking(false);
        resetMovementSpeed();

        Vec3 origin = modmob.getEyePosition();
        Vec3 look = modmob.getLookAngle().normalize();

        AABB attackBox = modmob.getBoundingBox()
                .expandTowards(look.scale(attackAction.range(scale)))
                .inflate(attackAction.range(scale) * 0.5, 0.75, attackAction.range(scale) * 0.5);

        List<LivingEntity> targets = modmob.level().getEntitiesOfClass(
                LivingEntity.class, attackBox,
                e -> e != modmob && e.isAlive() && !e.isSpectator()
        );

        float cosHalfAngle = (float) Math.cos(Math.toRadians(attackAction.arcAngle() / 2));
        float damage = (float) modmob.getAttributeValue(Attributes.ATTACK_DAMAGE);

        for (LivingEntity entity : targets) {
            Vec3 toTarget = entity.getEyePosition().subtract(origin).normalize();
            float dot = (float) look.dot(toTarget);
            if (dot > cosHalfAngle) {
                entity.hurt(modmob.damageSources().mobAttack(modmob), damage);
            }
        }
    }

    protected boolean isTargetInAttackArc () {
        LivingEntity target = modmob.getTarget();
        if (target == null) return false;

        double dx = target.getX() - modmob.getX();
        double dz = target.getZ() - modmob.getZ();
        double targetAngle = (Math.atan2(dz, dx) * (180 / Math.PI)) - 90;
        double mobYaw = modmob.getYRot() % 360;

        targetAngle = (targetAngle + 360) % 360;
        mobYaw = (mobYaw + 360) % 360;

        double relativeAngle = targetAngle - mobYaw;
        return relativeAngle >= -90 && relativeAngle <= 90;
    }

    protected void resetMovementSpeed () {
        Objects.requireNonNull(modmob.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(originalSpeed);
    }

    protected void resetAttackState () {
        attackChargeTick = 0;
        attackCooldown = 0;
        modmob.setAttacking(false);
        resetMovementSpeed();
    }
}
