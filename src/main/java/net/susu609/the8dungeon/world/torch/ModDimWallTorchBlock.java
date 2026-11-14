package net.susu609.the8dungeon.world.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.init.ModBlocksRegistry;

/** Đuốc tường leo lét: sau DIM_TICKS thì tắt hẳn thành wall_torch_off, giữ nguyên hướng. */
public class ModDimWallTorchBlock extends WallTorchBlock {

    public ModDimWallTorchBlock(Properties properties, ParticleOptions flameParticle) {
        super(properties, flameParticle);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
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
