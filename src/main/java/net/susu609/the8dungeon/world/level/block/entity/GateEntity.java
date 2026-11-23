package net.susu609.the8dungeon.world.level.block.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Cửa hầm ngục: prop 2x3x1 block, vẽ bằng GateModel.
 * Dữ liệu đồng bộ và lưu/đọc NBT dùng lại của PropEntity (không ghi đè).
 */
public class GateEntity extends PropEntity {

    // Hitbox khớp model "Gate" (32 x 48 x 16 px), sinh bởi plugin Blockbench java_single_texture.
    // Sửa model thì xuất lại và dán đè đoạn này.
    private static final Vec3 SIZE = new Vec3(2.0, 3.0, 1.0);   // rộng X, cao Y, sâu Z (block) khi yaw = 0
    private static final EntityDataAccessor<Boolean> DATA_OPEN = SynchedEntityData.defineId(GateEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LOCKED = SynchedEntityData.defineId(GateEntity.class, EntityDataSerializers.BOOLEAN);
    public final AnimationState openState = new AnimationState();
    public final AnimationState closeState = new AnimationState();
    public final AnimationState activeState = new AnimationState();
    private int activeUntil;          // tick mà rung phải dừng

    public GateEntity(EntityType<? extends PropEntity> type, Level level) {
        super(type, level);
    }

    public boolean isOpen() {
        return this.entityData.get(DATA_OPEN);
    }

    public void setOpen(boolean open) {
        this.entityData.set(DATA_OPEN, open);
    }

    public boolean isLocked() {
        return this.entityData.get(DATA_LOCKED);
    }

    public void setLocked(boolean locked) {
        this.entityData.set(DATA_LOCKED, locked);
        if (locked) setOpen(false);                      // khoá là sập cửa
    }

    @Override
    protected Vec3 size() {
        return SIZE;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide && !isLocked()) setOpen(!isOpen());
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (activeState == null) return;   // đang dựng dở, bỏ qua
        if (DATA_OPEN.equals(key) && level().isClientSide) {
            if (isOpen()) {
                closeState.stop();
                openState.start(tickCount);
            } else {
                openState.stop();
                closeState.start(tickCount);
            }
            activeState.start(tickCount);                        // ◄ phải nằm ở đây
            activeUntil = tickCount + (isOpen() ? 20 : 10);      // ◄ và ở đây
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && activeState.isStarted() && tickCount >= activeUntil) {
            activeState.stop();
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();          // giữ DATA_STATE của PropEntity
        this.entityData.define(DATA_OPEN, false);
        this.entityData.define(DATA_LOCKED, false);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isOpen();
    }

}
