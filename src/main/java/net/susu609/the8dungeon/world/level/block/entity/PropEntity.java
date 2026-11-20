package net.susu609.the8dungeon.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PropEntity extends Entity {
    // Client cần biết vẽ block nào -> dữ liệu đồng bộ
    private static final EntityDataAccessor<BlockState> DATA_STATE = SynchedEntityData.defineId(PropEntity.class, EntityDataSerializers.BLOCK_STATE);

    public PropEntity(EntityType<? extends PropEntity> type, Level level) {
        super(type, level);
        // TODO: không rơi -> setNoGravity(...)
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_STATE, Blocks.STONE_BRICKS.defaultBlockState());
    }

    public BlockState getBlockState() { /* TODO: entityData.get(...) */
        return this.entityData.get(DATA_STATE);
    }

    public void setBlockState(BlockState state) { /* TODO: entityData.set(...) */
        this.entityData.set(DATA_STATE, state);
    }

    // Lưu / đọc khi chunk được ghi xuống đĩa
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // TODO: tag.put("BlockState", NbtUtils.writeBlockState(...))
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            //noinspection resource
            this.setBlockState(NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        }
    }
    // Cư xử như khối
    @Override
    public boolean canBeCollidedWith() { /* TODO: không đi xuyên, đứng lên được */
        return true;
    }

    @Override
    public boolean isPickable() { /* TODO: nhắm chuột trúng, tương tác được */
        return true;
    }

    @Override
    public boolean isPushable() { /* TODO: không bị đẩy */
        return false;
    }
}