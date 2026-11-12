package net.susu609.the8dungeon.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.init.ModSoundsRegistry;

public class ClawdEntity extends PathfinderMob {
    public ClawdEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        // Thêm goal sau: đi theo người chơi, nhìn người chơi, đi lang thang
    }

    // Giữ nguyên defineSynchedData / readAdditionalSaveData / addAdditionalSaveData


    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {

    }
    @Override
    protected SoundEvent getAmbientSound() { return ModSoundsRegistry.CLAWD_AMBIENT.get(); }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return ModSoundsRegistry.CLAWD_HURT.get(); }

    @Override
    protected SoundEvent getDeathSound() { return ModSoundsRegistry.CLAWD_DEATH.get(); }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSoundsRegistry.CLAWD_STEP.get(), 0.4F, 1.0F);
    }

    @Override
    public float getVoicePitch() { return 1.15F; } // giọng cao hơn một chút

    // Gọi từ AI/hành vi sau này:
    public void playWorkSound()  { this.playSound(ModSoundsRegistry.CLAWD_WORK.get(), 0.8F, 0.95F + this.random.nextFloat() * 0.1F); }
    public void playThinkSound() { this.playSound(ModSoundsRegistry.CLAWD_THINK.get(), 0.6F, 1.0F); }
    public void playHappySound() { this.playSound(ModSoundsRegistry.CLAWD_HAPPY.get(), 0.9F, 1.0F); }
}
