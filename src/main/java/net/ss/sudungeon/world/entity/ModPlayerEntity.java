package net.ss.sudungeon.world.entity;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.client.animation.definitions.ModPlayerAnimation;
import net.ss.sudungeon.util.Log;
import net.ss.sudungeon.util.Vars;
import net.ss.sudungeon.init.SsModEntities;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ModPlayerEntity extends Monster {

    private UUID realPlayerUUID;
    private final Map<AnimationDefinition, AnimationState> animationStates = new HashMap<>();

    private final AnimationState attackAnimationState = new AnimationState();
    private final AnimationState hurtAnimationState = new AnimationState();
    private final AnimationState idleAnimationState = new AnimationState();

    private AnimationDefinition currentMovementAnimation = ModPlayerAnimation.PLAYER_IDLE;
    private AnimationDefinition currentAttackAnimation = null;
    private AttackAction currentAttackAction = AttackAction.PLAYER_ATTACK_1;

    public static final EntityDataAccessor<String> CHARACTER = SynchedEntityData.defineId(ModPlayerEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> IS_SLIM = SynchedEntityData.defineId(ModPlayerEntity.class, EntityDataSerializers.BOOLEAN);

    // Constructors
    public ModPlayerEntity(EntityType<ModPlayerEntity> type, Level level) {
        super(type, level);
    }

    public ModPlayerEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(SsModEntities.MOD_PLAYER.get(), level);
    }

    // Attributes
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    // Sync animation state from player
    public void syncWithPlayer(LocalPlayer player) {
        var vars = Vars.PP(player);
        this.entityData.set(CHARACTER, vars.character);
        this.entityData.set(IS_SLIM, vars.isSlim);

        // Hurt
        if (player.hurtTime > 0 && !hurtAnimationState.isStarted()) {
            hurtAnimationState.start(player.tickCount);
        }

        // Movement
        boolean isWalking = player.getDeltaMovement().horizontalDistanceSqr() > 0.002;
        if (isWalking && currentMovementAnimation != ModPlayerAnimation.PLAYER_WALK) {
            setMovementAnimationState(ModPlayerAnimation.PLAYER_WALK);
        } else if (!isWalking && currentMovementAnimation != ModPlayerAnimation.PLAYER_IDLE) {
            setMovementAnimationState(ModPlayerAnimation.PLAYER_IDLE);
        }

        // Attack
        boolean isAttacking = Vars.PP(player).isAttacking;
        if (isAttacking && currentAttackAnimation != ModPlayerAnimation.PLAYER_SWORD_ATTACK1) {
            setAttackAnimationState(ModPlayerAnimation.PLAYER_SWORD_ATTACK1);
        } else if (!isAttacking && currentAttackAnimation != null) {
            setAttackAnimationState(null);
        }
    }

    public ResourceLocation getSkin() {
        boolean slim = this.entityData.get(IS_SLIM);
        String path = slim ? "slim/" : "wide/";
        return new ResourceLocation("ss", "textures/entity/player/" + path + this.entityData.get(CHARACTER) + ".png");
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARACTER, "steve");
        this.entityData.define(IS_SLIM, false);
    }

    // Animation Setters
    private void setMovementAnimationState(AnimationDefinition anim) {
        this.currentMovementAnimation = anim;
        this.idleAnimationState.stop();
        if (anim == ModPlayerAnimation.PLAYER_IDLE) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    private void setAttackAnimationState(AnimationDefinition anim) {
        this.currentAttackAnimation = anim;
        if (anim == null) attackAnimationState.stop();
        else attackAnimationState.start(this.tickCount);
    }

    public void setAttackAnimation(AttackAction action) {
        if (this.currentAttackAction != action) {
            this.currentAttackAction = action;
        }
        if (attackAnimationState.isStarted()) attackAnimationState.stop();
        attackAnimationState.start(this.tickCount);
        Log.i("ModPlayerEntity", "Set attack animation: " + action.name());
    }

    // Animation Access
    private AnimationState getAnimationState(AnimationDefinition anim) {
        return animationStates.computeIfAbsent(anim, k -> new AnimationState());
    }

    public @NotNull AnimationState getAttackAnimationState() {
        return attackAnimationState;
    }

    public @NotNull AnimationState getHurtAnimationState() {
        return hurtAnimationState;
    }

    public @NotNull AnimationState getIdleAnimationState() {
        return idleAnimationState;
    }

    public AnimationDefinition getCurrentAnimation() {
        return currentAttackAnimation;
    }

    public AttackAction getCurrentAttackAction() {
        return currentAttackAction;
    }

    public boolean isWalking() {
        return getDeltaMovement().horizontalDistanceSqr() > 0.0001;
    }

    // UUID Sync
    public UUID getRealPlayerUUID() {
        return realPlayerUUID;
    }

    public void setRealPlayerUUID(UUID uuid) {
        this.realPlayerUUID = uuid;
    }

}
