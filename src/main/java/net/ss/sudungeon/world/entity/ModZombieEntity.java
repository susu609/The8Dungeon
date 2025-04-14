package net.ss.sudungeon.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.init.SsModEntities;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.world.entity.ai.goal.ModZombieMeleeAttackGoal;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;


public class ModZombieEntity extends ModMonster {
    public final AnimationState summonAnimationState = new AnimationState();

    public ModZombieEntity(EntityType<ModZombieEntity> type, Level level) {
        super(type, level);
        registerGoals();
    }

    public ModZombieEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(SsModEntities.MOD_ZOMBIE.get(), level);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ModZombieMeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damagesource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    protected void playStepSound(@NotNull BlockPos blockpos, @NotNull BlockState blockstate) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ModMonster.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(SsModAttributes.ATTACK_REACH.get(), 1);
    }

    @Override
    public boolean isBoss() {
        return false;
    }
}
