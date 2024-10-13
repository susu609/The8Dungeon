package net.ss.sudungeon.world.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.ss.sudungeon.init.SsModEntities;
import org.jetbrains.annotations.NotNull;

import static net.ss.sudungeon.SsMod.LOGGER;

public class TargetDummy extends Monster {
    private static final EntityDataAccessor<Boolean> BOSS = SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.BOOLEAN);
    public AnimationState hurtAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    private int healthRechargeTimer = 0;
    private PlayersTracker playersTracker;


    public TargetDummy (PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(SsModEntities.TARGET_DUMMY.get(), level);
    }

    public TargetDummy (EntityType<? extends Monster> type, Level world) {
        super(type, world);
        setMaxUpStep(0.6f);
        xpReward = 0;
        setNoAi(true);
    }


    // Tạo thanh máu Boss
    public void setBoss (boolean isBoss) {
        this.entityData.set(BOSS, isBoss);
        this.playersTracker.showHealthBar(isBoss);
    }

    public boolean isBoss () {
        return this.entityData.get(BOSS);
    }

    // Xử lý sự kiện khi thực thể nhận sát thương

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            if (this.healthRechargeTimer == 0 && this.getHealth() - amount <= 1) {
                this.setHealth(1);  // Không chết, giữ 1 máu
                this.setRecharging();  // Bắt đầu hồi máu
                return true;  // Ngăn chặn việc chết
            }
            // Phát sự kiện đến client để kích hoạt hoạt ảnh "hurt"
            this.level().broadcastEntityEvent(this, (byte) 3);
//            LOGGER.info("[TargetDummy] Event 33 broadcast.");
        }
        return super.hurt(source, amount);
    }

    public boolean isHurt () {
        // Trả về true nếu hoạt ảnh hurt đang chạy
        return this.hurtAnimationState.isStarted();
    }

    // Bắt đầu hồi máu
    private void setRecharging () {
        this.healthRechargeTimer = 200;  // Hồi trong 10 giây
        this.level().broadcastEntityEvent(this, (byte) 3);  // Mã số 33 là một mã giả lập cho hoạt ảnh hurt
    }

    @Override
    public void handleEntityEvent (byte id) {
        if (id == 3) {  // Mã số 33 tương ứng với hurt animation
            this.hurtAnimationState.start(this.tickCount);  // Kích hoạt hoạt ảnh "hurt" phía client
//            LOGGER.info("[TargetDummy] hurtAnimationState.start.");
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep () {
        super.aiStep();
        if (this.healthRechargeTimer > 0) {
            --this.healthRechargeTimer;
            if (this.healthRechargeTimer == 0) {
                this.setHealth(this.getMaxHealth());  // Hồi đầy máu khi hết thời gian
                this.playersTracker.clear();  // Xóa danh sách người chơi
            }
        }
    }

    // Theo dõi người chơi tấn công
    private class PlayersTracker {
        private final ServerBossEvent bossEvent;

        private PlayersTracker () {
            this.bossEvent = new ServerBossEvent(TargetDummy.this.getDisplayName(), BossBarColor.GREEN, BossBarOverlay.NOTCHED_10);
        }

        public void showHealthBar (boolean show) {
            if (show) {
                this.bossEvent.addPlayer((ServerPlayer) TargetDummy.this.level().players());
            } else {
                this.bossEvent.removeAllPlayers();
            }
        }

        public void clear () {
            this.bossEvent.removeAllPlayers();
        }
    }

    public static AttributeSupplier.Builder createAttributes () {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 2004);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 16);

        return builder;
    }

}
