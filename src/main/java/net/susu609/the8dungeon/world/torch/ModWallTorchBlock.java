package net.susu609.the8dungeon.world.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.susu609.the8dungeon.world.level.block.entity.ModTorchBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Đuốc tường có nhiên liệu: FUEL từ 0 (tắt) đến 4 (cháy đầy).
 * Kế thừa WallTorchBlock nên giữ nguyên hướng gắn (FACING) của vanilla.
 */
public class ModWallTorchBlock extends WallTorchBlock implements EntityBlock {

    // Phải là hằng static: Block đọc nó ngay khi dựng, trước cả constructor của lớp này
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, 4);

    public ModWallTorchBlock(Properties properties, ParticleOptions particle) {
        super(properties, particle);
        // Giữ hướng mặc định của cha, thêm FUEL = 4 (cháy đầy khi vừa đặt)
        this.registerDefaultState(this.defaultBlockState().setValue(FUEL, 4));
    }

    // Khai báo các thuộc tính của khối này
    @Override
    protected void createBlockStateDefinition(
            StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // giữ lại FACING của vanilla
        builder.add(FUEL);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        // Mỗi khi đặt đuốc, Minecraft gọi hàm này để tạo bộ đếm nhiên liệu đi kèm
        return new ModTorchBlockEntity(pos, state);
    }
}