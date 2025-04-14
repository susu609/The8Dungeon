/*
package net.ss.sudungeon.world.level.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.ss.sudungeon.init.SsModBlockEntities;

public class ConverterBlockEntity extends BlockEntity {

    private String nextBlockId = ""; // Lưu ID của block để chuyển đổi
    private boolean isConverted = false; // Trạng thái đã chuyển đổi hay chưa

    public ConverterBlockEntity(BlockPos pos, BlockState state) {
        super(SsModBlockEntities.CONVERTER_BLOCK_ENTITY.get(), pos, state);
    }

    // Getter và Setter cho `nextBlockId`
    public String getNextBlockId() {
        return nextBlockId;
    }

    public void setNextBlockId(String nextBlockId) {
        this.nextBlockId = nextBlockId;
    }

    // Getter và Setter cho `isConverted`
    public boolean isConverted() {
        return isConverted;
    }

    public void setConverted(boolean converted) {
        this.isConverted = converted;
    }

    // Ghi dữ liệu vào NBT
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("nextBlockId", nextBlockId);
        tag.putBoolean("isConverted", isConverted);
    }

    // Đọc dữ liệu từ NBT
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.nextBlockId = tag.getString("nextBlockId");
        this.isConverted = tag.getBoolean("isConverted");
    }
}*/
