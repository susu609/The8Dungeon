package net.ss.sudungeon.world.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.init.SsModEntities;
import net.ss.sudungeon.world.entity.ai.goal.ModZombieMeleeAttackGoal;
import org.jetbrains.annotations.NotNull;

public class ModZombie extends Monster {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int attackCooldown = 0;
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
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick () {
        super.tick();

        // Giảm thuộc tính di chuyển xuống 0 nếu đang ở hoạt ảnh hurt hoặc attack
        if (hurtAnimationState.isStarted() || attackAnimationState.isStarted()) {
            idleAnimationState.stop();
        }

        // Quản lý trạng thái chờ khi không có mục tiêu
        if (this.getTarget() == null && !idleAnimationState.isStarted() && firstIdle) {
            this.level().broadcastEntityEvent(this, (byte) 1); // Hoạt ảnh chờ lần đầu
            firstIdle = false;
        }

    }


    @Override
    public boolean hurt (@NotNull DamageSource source, float amount) {
        if (!this.level().isClientSide && !hurtAnimationState.isStarted() && !attackAnimationState.isStarted()) {
            this.level().broadcastEntityEvent(this, (byte) 2);
        }
        return super.hurt(source, amount);
    }

    private void stopAllAnimations () {
        idleAnimationState.stop();
        hurtAnimationState.stop();
        attackAnimationState.stop();
    }

    @Override
    public void handleEntityEvent (byte id) {
//        stopAllAnimations(); // Dừng tất cả hoạt ảnh trước khi bắt đầu cái mới
        switch (id) {
            case 1 -> idleAnimationState.start(this.tickCount);

            case 2 -> {
                stopAllAnimations();
                hurtAnimationState.start(this.tickCount);
            }
            case 3 -> {
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
                .add(Attributes.ARMOR, 2)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(SsModAttributes.ATTACK_REACH.get(), 1)
                ;
    }
}
