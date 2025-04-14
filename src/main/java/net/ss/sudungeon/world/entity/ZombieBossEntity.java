package net.ss.sudungeon.world.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.init.SsModAttributes;
import net.ss.sudungeon.init.SsModEntities;
import net.ss.sudungeon.world.entity.ai.goal.ZombieBossCombatGoal;
import net.ss.sudungeon.world.entity.ai.goal.ZombieBossMeleeAttackGoal;
import net.ss.sudungeon.world.entity.ai.goal.ZombieGroundSlamAttackGoal;
import net.ss.sudungeon.world.entity.ai.goal.ZombieWideSlashAttackGoal;
import org.jetbrains.annotations.NotNull;

public class ZombieBossEntity extends ModMonster {

    private final ServerBossEvent bossBar = new ServerBossEvent(
            Component.literal("Rotten Champion"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public @NotNull AnimationState slashAnimationState = new AnimationState();
    public @NotNull AnimationState slamAnimationState = new AnimationState();

    private boolean performingSlam = false;
    private boolean performingWideSlash = false;

    public ZombieBossEntity (EntityType<? extends ZombieBossEntity> type, Level level) {
        super(type, level);
        registerGoals();
    }

    public ZombieBossEntity (PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(SsModEntities.ZOMBIE_BOSS.get(), level);
    }

    protected void registerGoals () {
        this.goalSelector.addGoal(1, new ZombieBossCombatGoal(this));
        this.goalSelector.addGoal(2, new ZombieGroundSlamAttackGoal(this));
        this.goalSelector.addGoal(3, new ZombieWideSlashAttackGoal(this));
        this.goalSelector.addGoal(4, new ZombieBossMeleeAttackGoal(this));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep () {
        super.customServerAiStep();
        bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public void startSeenByPlayer (Player player) {
        bossBar.addPlayer((ServerPlayer) player);
    }

    public void stopSeenByPlayer (Player player) {
        bossBar.removePlayer((ServerPlayer) player);
    }

    public void setPerformingSlam (boolean performingSlam) {
        this.performingSlam = performingSlam;
    }

    public void setPerformingWideSlash (boolean performingWideSlash) {
        this.performingWideSlash = performingWideSlash;
    }

    public boolean isPerformingSlam () {
        return performingSlam;
    }

    public boolean isPerformingWideSlash () {
        return performingWideSlash;
    }

    public static AttributeSupplier.Builder createAttributes () {
        return ModMonster.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 120)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(SsModAttributes.ATTACK_REACH.get(), 2);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 2 -> idleAnimationState.start(this.tickCount);
            case 4 -> {
                stopAllAnimations();
                attackAnimationState.start(this.tickCount);
            }
            case 5 -> {
                stopAllAnimations();
                slashAnimationState.start(this.tickCount);
            }
            case 6 -> {
                stopAllAnimations();
                slamAnimationState.start(this.tickCount);
            }
            case 33 -> hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean isBoss () {
        return true;
    }
}

