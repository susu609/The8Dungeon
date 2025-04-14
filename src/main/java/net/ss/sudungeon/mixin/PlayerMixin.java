package net.ss.sudungeon.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ss.sudungeon.world.entity.ModMonster;
import net.ss.sudungeon.world.entity.player.IPlayerAnim;
import net.ss.sudungeon.world.entity.player.PlayerAttackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IPlayerAnim {

    @Unique
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(PlayerMixin.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private final AnimationState attackAnimationState = new AnimationState();
    @Unique
    public int attackAnimationTimeout = 30;

    @Unique
    private final AnimationState hurtAnimationState = new AnimationState();
    @Unique
    private final AnimationState drinkAnimationState = new AnimationState();
    @Unique
    private final AnimationState idleAnimationState = new AnimationState();
    @Unique
    public int idleAnimationTimeout = 20;

    @Unique
    private final AnimationState walkAnimationState = new AnimationState();
    @Unique
    private final AnimationState runAnimationState = new AnimationState();

    protected PlayerMixin (EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Override
    public AnimationState getAttackAnimState () {
        return attackAnimationState;
    }

    @Override
    public AnimationState getHurtAnimState () {
        return hurtAnimationState;
    }

    @Override
    public AnimationState getDrinkAnimState () {
        return drinkAnimationState;
    }

    @Override
    public AnimationState getIdleAnimState () {
        return idleAnimationState;
    }

    @Override
    public AnimationState getWalkAnimState () {
        return walkAnimationState;
    }

    @Override
    public AnimationState getRunAnimState () {
        return runAnimationState;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void defineSynchedData (CallbackInfo ci) {
        this.entityData.define(ATTACKING, false);
    }

    // Đọc dữ liệu từ server
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void ss$readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound, CallbackInfo ci) {
        this.entityData.set(ATTACKING, compound.getBoolean("Attacking"));
    }

    // Ghi dữ liệu vào server
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void ss$addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean("Attacking", this.entityData.get(ATTACKING));
    }
/*    @Inject(method = "hurt", at = @At("HEAD"))
    private void ss$onHurt(DamageSource p_36154_, float p_36155_, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;
        if (hurtAnimationState.shouldStart(ModPlayerAnimation.PLAYER_HURT)) {
            hurtAnimationState.start(ModPlayerAnimation.PLAYER_HURT, player.tickCount, false);
        }
    }*/

    @Inject(method = "tick", at = @At("HEAD"))
    private void ss$updateTickAnimations (CallbackInfo ci) {
        Player player = (Player) (Object) this;
        long tick = player.tickCount;

        if (player.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    public boolean isAttacking () {
        Player player = (Player) (Object) this;
        return player.getEntityData().get(ATTACKING);
    }

    @Unique
    protected void setupAnimationStates () {
        Player player = (Player) (Object) this;
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(player.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if (this.isAttacking()) {
            if (attackAnimationTimeout <= 0) {
                attackAnimationTimeout = 20;
                attackAnimationState.start(player.tickCount);
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

    @Unique
    protected void stopAllAnimations () {
        idleAnimationState.stop();
        hurtAnimationState.stop();
        attackAnimationState.stop();
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    public void handleEntityEvent (byte id, CallbackInfo ci) {
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

    /**
     * Inject vào lúc vừa bắt đầu đánh 1 entity.
     * Forge gọi `attackTargetEntityWithCurrentItem(...)` từ nhiều nơi.
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttackStart (Entity target, CallbackInfo ci) {
        Player self = (Player) (Object) this;

        if (!self.level().isClientSide()) return;
        if (!(self instanceof AbstractClientPlayer clientPlayer)) return;

        float cooldown = self.getAttackStrengthScale(0f);
        if (cooldown < 1.0f) return;

        PlayerAttackHandler.get(clientPlayer).onSwingInput(clientPlayer, true);
        self.level().broadcastEntityEvent(self, (byte) 4);
        ci.cancel();
    }


}
