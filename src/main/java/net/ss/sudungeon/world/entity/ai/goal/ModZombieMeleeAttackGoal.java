package net.ss.sudungeon.world.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.init.SsModSounds;
import net.ss.sudungeon.world.entity.ModZombie;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class ModZombieMeleeAttackGoal extends MeleeAttackGoal {
    private final ModZombie modZombie;
    private final double localSpeedModifier;
    private static final int ATTACK_DELAY = 10;
    private static final int ATTACK_COOLDOWN = 20;
    private static final double ATTACK_AREA_RADIUS = 1;
    private int attackChargeTick = 0;
    private int attackCooldown = 0;

    public ModZombieMeleeAttackGoal(ModZombie mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.modZombie = mob;
        this.localSpeedModifier = speedModifier;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.mob.getTarget() == null) {
            return;
        }

        double squaredDistance = this.mob.distanceToSqr(this.mob.getTarget());

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (squaredDistance <= getAttackReachSqr()) {
            this.mob.getLookControl().setLookAt(this.mob.getTarget(), 30.0F, 30.0F);

            if (attackChargeTick >= ATTACK_DELAY) {
                if (isTargetInAttackArc()) {
                    performAttack();
                    attackChargeTick = 0;
                }
            } else {
                attackChargeTick++;
            }
        } else {
            attackChargeTick = 0;
            this.mob.getNavigation().moveTo(this.mob.getTarget(), this.localSpeedModifier);
        }
    }

    public void performAttack() {
        if (attackCooldown > 0) return;

        Vec3 center = modZombie.position().add(modZombie.getLookAngle().normalize().scale(0.5));
        List<Entity> entitiesFound = modZombie.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(ATTACK_AREA_RADIUS), e -> e != modZombie && modZombie.hasLineOfSight(e))
                .stream().sorted(Comparator.comparingDouble(ent -> ent.distanceToSqr(center))).toList();

        for (Entity targetEntity : entitiesFound) {
            if (targetEntity instanceof LivingEntity) {
                targetEntity.hurt(modZombie.damageSources().mobAttack(modZombie), (float) modZombie.getAttributeValue(Attributes.ATTACK_DAMAGE));
                modZombie.playSound(SsModSounds.ZOMBIE_ATTACK.get(), 1.0F, modZombie.getVoicePitch());
                targetEntity.setDeltaMovement(new Vec3(modZombie.getLookAngle().x * 0.35, 0.25, modZombie.getLookAngle().z * 0.35));
            }
        }

        attackCooldown = ATTACK_COOLDOWN;
        this.modZombie.level().broadcastEntityEvent(this.modZombie, (byte) 3);
    }

    private boolean isTargetInAttackArc() {
        Entity target = this.mob.getTarget();
        if (target == null) return false;

        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        double targetAngle = (Math.atan2(dz, dx) * (180 / Math.PI)) - 90;
        double mobYaw = this.mob.getYRot() % 360;

        if (targetAngle < 0) {
            targetAngle += 360;
        }

        if (mobYaw < 0) {
            mobYaw += 360;
        }

        double relativeAngle = targetAngle - mobYaw;
        return relativeAngle >= -45 && relativeAngle <= 45;
    }

    private double getAttackReachSqr() {
        double attackReach = modZombie.getAttributeValue(SsModAttributes.ATTACK_REACH.get());
        return attackReach * attackReach;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && attackCooldown == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && attackCooldown == 0;
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target, double squaredDistance) {
    }
}