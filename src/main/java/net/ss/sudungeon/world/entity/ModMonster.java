package net.ss.sudungeon.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.util.Vars;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public abstract class ModMonster extends Monster {

    protected static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(ModMonster.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public int idleAnimationTimeout = 20;

    public final AnimationState hurtAnimationState = new AnimationState();
    public int hurtAnimationTimeout = 0;

    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 30;

    protected ModMonster(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    protected ModMonster(PlayMessages.SpawnEntity spawnEntity, EntityType<? extends Monster> type, Level level) {
        this(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    protected void stopAllAnimations() {
        idleAnimationState.stop();
        hurtAnimationState.stop();
        attackAnimationState.stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    protected void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if (this.isAttacking()) {
            if (attackAnimationTimeout <= 0) {
                attackAnimationTimeout = 20;
                attackAnimationState.start(this.tickCount);
            } else {
                --this.attackAnimationTimeout;
            }
        } else {
            if (attackAnimationTimeout > 0) {
                attackAnimationTimeout = 0;
                attackAnimationState.stop();
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 2 -> idleAnimationState.start(this.tickCount);
            case 4 -> {
                stopAllAnimations();
                attackAnimationState.start(this.tickCount);
            }
            case 33 -> hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
        this.level().broadcastEntityEvent(this, (byte) 2);
        Vars.setGM(world, vars -> vars.enemyCount++); // đảm bảo đồng bộ
        return retval;
    }

    @Override
    public void die(@NotNull DamageSource source) {
        stopAllAnimations();
        Vars.setGM(this.level(), vars -> vars.enemyCount--); // dùng level() vì world không có sẵn
        super.die(source);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            Entity attacker = source.getEntity();

            // Chỉ giảm sát thương nếu bị ModMonster đánh
            if (attacker instanceof ModMonster) {
                switch (this.level().getDifficulty()) {
                    case EASY -> amount *= 0.75f;
                    case NORMAL -> amount *= 0.50f;
                    case HARD -> amount *= 0.25f;
                }
            }
        }
        return super.hurt(source, amount);
    }


    public abstract boolean isBoss();
}
