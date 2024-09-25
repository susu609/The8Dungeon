package net.ss.sudungeon.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

public class ColossalZombie extends Zombie {

    public ColossalZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();  // Zombie này không thể despawn
    }

    // Thiết lập kích thước lớn hơn
    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(1.2F, 3.9F); // Kích thước x2
    }

    // Tạo các thuộc tính cho ColossalZombie
    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, SpawnGroupData spawnData, CompoundTag data) {
        super.finalizeSpawn(world, difficulty, reason, spawnData, data);
        this.setHealth(this.getMaxHealth());  // Đặt máu của thực thể bằng giá trị max health
        return spawnData;
    }


    // Đăng ký các mục tiêu tấn công
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));  // Tấn công cận chiến
        // Các mục tiêu khác có thể thêm vào đây
    }
}
