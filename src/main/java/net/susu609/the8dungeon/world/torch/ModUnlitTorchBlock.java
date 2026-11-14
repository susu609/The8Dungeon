package net.susu609.the8dungeon.world.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Đuốc đứng đã tắt: không phát sáng, không phun lửa hay khói. */
public class ModUnlitTorchBlock extends TorchBlock {

    public ModUnlitTorchBlock(Properties properties, ParticleOptions flameParticle) {
        super(properties, flameParticle);
    }

    /** Bỏ hạt lửa và khói của vanilla. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    }
}
