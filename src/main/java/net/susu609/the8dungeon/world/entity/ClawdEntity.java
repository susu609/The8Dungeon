package net.susu609.the8dungeon.world.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.init.ModSoundsRegistry;
import net.susu609.the8dungeon.world.item.ClawdSpawnEggItem;

/** Clawd: linh vật agent thợ xây. Mỗi người chơi chỉ có một con. */
public class ClawdEntity extends TamableAnimal {

    public ClawdEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1D, 6.0F, 2.0F, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    // ===== Chủ nhân: nhấp chuột phải tay không để ngồi / đi theo =====

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && this.isOwnedBy(player) && player.getItemInHand(hand).isEmpty()) {
            if (!this.level().isClientSide) {
                boolean sit = !this.isOrderedToSit();
                this.setOrderedToSit(sit);
                this.navigation.stop();
                this.setTarget(null);
                this.playHappySound();
                player.displayClientMessage(Component.literal(
                        sit ? "Clawd: mình đứng đây chờ nhé." : "Clawd: đi thôi!"), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // ===== Một con duy nhất: tự biến mất nếu không còn là Clawd đang hoạt động của chủ =====

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 100 == 0
                && this.getOwner() instanceof Player owner) {
            UUID active = ClawdSpawnEggItem.activeClawdOf(owner);
            if (active != null && !active.equals(this.getUUID())) {
                this.discard();
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ===== Chiến đấu =====

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper) return false;
        if (target instanceof TamableAnimal pet && pet.isOwnedBy(owner)) return false;
        if (target instanceof Player p && owner instanceof Player o && !o.canHarmPlayer(p)) return false;
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) this.playWorkSound();
        return hit;
    }

    // ===== Không sinh sản =====

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    // ===== Âm thanh =====

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundsRegistry.CLAWD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundsRegistry.CLAWD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundsRegistry.CLAWD_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Mức âm lượng của mob vanilla
        this.playSound(ModSoundsRegistry.CLAWD_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public float getVoicePitch() {
        // Giữ biến thiên ngẫu nhiên của vanilla, chỉ nâng nhẹ cao độ
        return super.getVoicePitch() * 1.15F;
    }

    public void playWorkSound() {
        this.playSound(ModSoundsRegistry.CLAWD_WORK.get(), 0.8F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    public void playThinkSound() {
        this.playSound(ModSoundsRegistry.CLAWD_THINK.get(), 0.6F, 1.0F);
    }

    public void playHappySound() {
        this.playSound(ModSoundsRegistry.CLAWD_HAPPY.get(), 0.9F, 1.0F);
    }
}
