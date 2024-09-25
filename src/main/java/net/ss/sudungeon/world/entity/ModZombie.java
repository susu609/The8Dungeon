package net.ss.sudungeon.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModZombie extends Zombie {

    public ModZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, SpawnGroupData spawnData, CompoundTag data) {
        super.finalizeSpawn(world, difficulty, reason, spawnData, data);
        this.setBaby(false);  // Zombie trưởng thành
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20.0D);  // Tăng máu
        this.setHealth(20.0F);  // Cài đặt lại máu
        return spawnData;
    }


    // Đăng ký các mục tiêu tấn công
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this)); // Thêm hành vi bơi
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false)); // Tấn công cận chiến
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D)); // Tránh nước và đi lang thang
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F)); // Nhìn người chơi
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this)); // Nhìn xung quanh ngẫu nhiên
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT; // Âm thanh tùy chỉnh khi không làm gì
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ZOMBIE_HURT; // Âm thanh khi bị thương
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH; // Âm thanh khi chết
    }

}
