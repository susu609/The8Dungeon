package net.susu609.the8dungeon.world.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.init.ModBlocksRegistry;

/** Đuốc đứng leo lét: sau DIM_TICKS thì tắt hẳn thành torch_off. */
public class ModDimTorchBlock extends TorchBlock {

    public ModDimTorchBlock(Properties properties, ParticleOptions flameParticle) {
        super(properties, flameParticle);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        // Chỉ lên lịch khi vừa được đặt ở server (không phải đổi thuộc tính của chính khối này)
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            level.scheduleTick(pos, this, TorchBurn.DIM_TICKS);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TorchBurn.convert(level, pos, state,
                ModBlocksRegistry.TORCH_OFF.get(), ModBlocksRegistry.WALL_TORCH_OFF.get(), true);
    }
}
