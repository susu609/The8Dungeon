package net.susu609.the8dungeon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.init.ModBlocksRegistry;
import net.susu609.the8dungeon.world.torch.TorchBurn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chèn logic cháy vào đuốc vanilla (torch và wall_torch).
 * Nhắm vào BlockBehaviour vì TorchBlock không tự khai báo onPlace và tick,
 * nên không thể @Inject trực tiếp vào TorchBlock.
 * Chỉ tác động đúng hai khối vanilla: RedstoneTorchBlock, soul_torch... không bị ảnh hưởng.
 */
@Mixin(BlockBehaviour.class)
public abstract class ModBlockBehaviour {

    /** Khi đuốc vanilla vừa được đặt ở server, lên lịch tick để đuốc cháy hết sau LIT_TICKS. */
    @Inject(method = "onPlace", at = @At("HEAD"))
    private void susu609$scheduleBurnOut(BlockState state, Level level, BlockPos pos,
                                         BlockState oldState, boolean moving, CallbackInfo ci) {
        if (level.isClientSide) return;
        if (!state.is(Blocks.TORCH) && !state.is(Blocks.WALL_TORCH)) return;
        if (oldState.is(state.getBlock())) return;
        level.scheduleTick(pos, state.getBlock(), TorchBurn.LIT_TICKS);
    }

    /** Khi tick đến hạn, đổi đuốc vanilla thành đuốc tắt (giữ hướng nếu là đuốc tường). */
    @Inject(method = "tick", at = @At("HEAD"))
    private void susu609$burnOut(BlockState state, ServerLevel level, BlockPos pos,
                                 RandomSource random, CallbackInfo ci) {
        if (!state.is(Blocks.TORCH) && !state.is(Blocks.WALL_TORCH)) return;
        TorchBurn.convert(level, pos, state,
                ModBlocksRegistry.TORCH_OFF.get(), ModBlocksRegistry.WALL_TORCH_OFF.get(), true);
    }
}
