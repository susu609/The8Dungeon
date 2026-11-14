package net.susu609.the8dungeon.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.init.ModBlockEntitiesRegistry;
import org.jetbrains.annotations.NotNull;

import static net.susu609.the8dungeon.world.torch.ModWallTorchBlock.FUEL;

public class ModTorchBlockEntity extends BlockEntity {

    // Hằng số: static final, giá trị cố định cho mọi đuốc
    public static final int MAX_TICKS = 12000;

    // Của riêng từng đuốc: KHÔNG static
    private int fuelTicks = MAX_TICKS;

    public ModTorchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public ModTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntitiesRegistry.TORCH.get(), pos, state);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, ModTorchBlockEntity be) {
        if (level.isClientSide()) return;        // chỉ server đếm
        if (be.fuelTicks <= 0) return;           // hết nhiên liệu thì thôi
        be.fuelTicks--;

        // Mức 0..4, làm tròn lên
        int stage = (be.fuelTicks * 4 + MAX_TICKS - 1) / MAX_TICKS;

        // Chỉ đổi khối khi mức thật sự đổi
        if (stage != state.getValue(FUEL)) {
            level.setBlock(pos, state.setValue(FUEL, stage), Block.UPDATE_ALL);
        }
        be.setChanged();                         // báo cho chunk: dữ liệu đã đổi, nhớ lưu
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("fuelTicks", fuelTicks);      // ghi số CÒN LẠI, không phải MAX_TICKS
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        fuelTicks = tag.getInt("fuelTicks");     // đọc lại
    }
}