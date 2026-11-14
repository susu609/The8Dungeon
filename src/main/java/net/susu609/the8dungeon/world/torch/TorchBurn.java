package net.susu609.the8dungeon.world.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Thời gian cháy và việc đổi khối của đuốc: torch (sáng) -> torch_off (tắt).
 * Đuốc đứng và đuốc tường dùng chung các hàm này.
 */
public final class TorchBurn {

    /** Số tick đuốc cháy trước khi tắt hẳn: 12000 tick = 10 phút. */
    public static final int LIT_TICKS = 12000;

    private TorchBurn() {}

    /**
     * Đổi khối tại pos sang khối đích, giữ hướng nếu là đuốc tường.
     *
     * @param standingTo khối đích nếu khối hiện tại là đuốc đứng
     * @param wallTo     khối đích nếu khối hiện tại là đuốc tường
     * @param extinguish true thì phát tiếng xì và hạt khói
     */
    public static void convert(ServerLevel level, BlockPos pos, BlockState from,
                               Block standingTo, Block wallTo, boolean extinguish) {
        boolean wall = from.getBlock() instanceof WallTorchBlock;
        BlockState to = (wall ? wallTo : standingTo).defaultBlockState();
        if (wall) {
            to = to.setValue(WallTorchBlock.FACING, from.getValue(WallTorchBlock.FACING));
        }
        level.setBlock(pos, to, Block.UPDATE_ALL);

        if (extinguish) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
            level.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    6, 0.1, 0.1, 0.1, 0.01);
        }
    }
}
