package net.ss.sudungeon.world.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.init.SsModEntities;
import net.ss.sudungeon.init.SsModSounds;
import net.ss.sudungeon.world.entity.ai.goal.ModZombieMeleeAttackGoal;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class ModZombie extends Monster {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private static final int ATTACK_COOLDOWN = 20;
    private static final int ATTACK_DELAY = 10; // Số tick trước khi gây sát thương
    private int attackCooldown = 0;
    private int attackChargeTick = 0;
    private boolean firstIdle = true; // Biến để kiểm tra trạng thái lần đầu

    public ModZombie (EntityType<? extends Monster> type, Level level) {
        super(type, level);
        registerGoals();
    }

    public ModZombie (PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(SsModEntities.MOD_ZOMBIE.get(), level);
    }

    protected void registerGoals () {
        this.goalSelector.addGoal(1, new ModZombieMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick () {
        super.tick();
        handleAttackCooldown();

        updateGoals();
        // Dừng di chuyển nếu đang ở hoạt ảnh hurt hoặc attack
        if (hurtAnimationState.isStarted() || attackAnimationState.isStarted()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO); // Dừng mọi chuyển động
            return;
        }


        // Quản lý trạng thái chờ khi không có mục tiêu
        if (this.getTarget() == null && !idleAnimationState.isStarted() && firstIdle) {
            this.level().broadcastEntityEvent(this, (byte) 1); // Hoạt ảnh chờ lần đầu
            firstIdle = false;
        }

        // Đặt lại firstIdle khi có mục tiêu
        if (this.getTarget() != null) {
            firstIdle = true;
        }
    }

    protected void updateGoals () {
        if (hurtAnimationState.isStarted() || attackAnimationState.isStarted()) {
            this.goalSelector.disableControlFlag(Goal.Flag.MOVE); // Tắt tạm di chuyển
        } else {
            this.goalSelector.enableControlFlag(Goal.Flag.MOVE); // Kích hoạt lại khi không còn tấn công hoặc bị thương
        }
    }


    private void handleAttackCooldown () {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }

    @Override
    public boolean hurt (@NotNull DamageSource source, float amount) {
        if (!this.level().isClientSide && !hurtAnimationState.isStarted() && !attackAnimationState.isStarted()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
        }
        return super.hurt(source, amount);
    }

    public void performAttack () {
        if (attackCooldown > 0) return;

        if (attackChargeTick >= ATTACK_DELAY) {
            Vec3 center = this.position().add(this.getLookAngle().normalize().scale(0.5));
            List<Entity> entitiesFound = this.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(0.5), e -> e != this && this.hasLineOfSight(e))
                    .stream().sorted(Comparator.comparingDouble(ent -> ent.distanceToSqr(center))).toList();

            for (Entity targetEntity : entitiesFound) {
                targetEntity.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                this.playSound(SsModSounds.ZOMBIE_ATTACK.get(), 1.0F, this.getVoicePitch());
                targetEntity.setDeltaMovement(new Vec3(this.getLookAngle().x * 0.35, 0.25, this.getLookAngle().z * 0.35));
            }

            attackChargeTick = 0; // Reset lại biến đếm tấn công
            attackCooldown = ATTACK_COOLDOWN;
        } else {
            attackChargeTick++;
        }
    }

    private void stopAllAnimations () {
        idleAnimationState.stop();
        walkAnimationState.stop();
        hurtAnimationState.stop();
        attackAnimationState.stop();
    }

    @Override
    public void handleEntityEvent (byte id) {
        switch (id) {
            case 1 -> {
                if (!idleAnimationState.isStarted()) {
                    idleAnimationState.start(this.tickCount);
                }
            }
            case 2 -> {
                if (!walkAnimationState.isStarted()) {
                    walkAnimationState.start(this.tickCount);
                }
            }
            case 3 -> {
                stopAllAnimations();
                hurtAnimationState.start(this.tickCount);
            }
            case 4 -> {
                stopAllAnimations();
                attackAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    public static AttributeSupplier.Builder createAttributes () {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.FOLLOW_RANGE, 16);
    }
}
