package net.susu609.the8dungeon.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PropEntity extends Entity {
    // Client cần biết vẽ block nào -> dữ liệu đồng bộ
    private static final EntityDataAccessor<BlockState> DATA_STATE = SynchedEntityData.defineId(PropEntity.class, EntityDataSerializers.BLOCK_STATE);

    public PropEntity(EntityType<? extends PropEntity> type, Level level) {
        super(type, level);
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
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            //noinspection resource
            this.setBlockState(NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState")));
        }
    }

    /** Kích thước (rộng X, cao Y, sâu Z) tính bằng block, khi entity nhìn về nam (yaw = 0). */
    protected Vec3 size() {
        // mặc định: lấy từ sized(...) của EntityType -> bệ đỡ không phải sửa gì
        return new Vec3(this.getBbWidth(), this.getBbHeight(), this.getBbWidth());
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        Vec3 s = size();
        // TODO (1): làm tròn yaw về 0/90/180/270 rồi lấy "phần tư" 0..3
        //   gợi ý: int quarter = Math.floorMod(Math.round(this.getYRot() / 90F), 4);
        // TODO (2): nếu quarter lẻ (90 hoặc 270) thì đổi chỗ rộng và sâu
        //   gợi ý: double w = quarter % 2 == 0 ? s.x : s.z;   double d = quarter % 2 == 0 ? s.z : s.x;
        Vec3 p = this.position();   // tâm đáy entity
        // TODO (3): hộp quanh tâm đáy: x ± w/2, y đến y + cao, z ± d/2
        //   gợi ý: return new AABB(p.x - w / 2, p.y, p.z - d / 2, p.x + w / 2, p.y + s.y, p.z + d / 2);
    }

    @Override
    public void setYRot(float yRot) {
        super.setYRot(yRot);
        this.setBoundingBox(this.makeBoundingBox());   // đổi hướng thì tính lại hộp
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