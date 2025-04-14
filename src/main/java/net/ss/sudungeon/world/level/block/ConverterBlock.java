/*
package net.ss.sudungeon.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ConverterBlock extends Block implements EntityBlock {

    public ConverterBlock(Properties properties) {
        super(properties);
    }

    // Random tick để hỗ trợ chuyển đổi
    @Override
    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true; // Kích hoạt random tick
    }

    @Override
    public void randomTick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!level.isClientSide) {
            System.out.println("Random tick triggered at: " + pos);
        }
    }

    // Xác định block có block entity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConverterBlockEntity(pos, state); // Trả về BlockEntity
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ConverterBlockEntity converterEntity) {
                // Kiểm tra nếu block đã chuyển đổi
                if (converterEntity.isConverted()) {
                    return; // Block đã chuyển đổi xong, thoát
                }

                // Logic chuyển đổi block
                String nextBlockId = converterEntity.getNextBlockId();
                if (!nextBlockId.isEmpty()) {
                    Block nextBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation(nextBlockId));
                    if (nextBlock != null) {
                        level.setBlockAndUpdate(pos, nextBlock.defaultBlockState()); // Chuyển đổi khối
                        converterEntity.setConverted(true); // Đánh dấu đã chuyển đổi
                    }
                }
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ConverterBlockEntity converterEntity) {
                converterEntity.setNextBlockId("minecraft:stone"); // Giá trị mặc định
                ((ServerLevel) level).scheduleTick(pos, this, 10); // Tick mỗi 10 tick
            }
        }
    }
}*/
