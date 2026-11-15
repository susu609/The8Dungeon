package net.susu609.the8dungeon.mixin;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.susu609.the8dungeon.client.CutawayState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Khi dựng mesh chunk, khối nằm trong CutawayState được coi như không khí,
 * nên bị cắt khỏi hình (thế giới thật không đổi). Chỉ chạy ở client.
 */
@Mixin(RenderChunkRegion.class)
public abstract class RenderChunkRegionMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void susu609$cutaway(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (CutawayState.isHidden(pos)) cir.setReturnValue(Blocks.AIR.defaultBlockState());
    }
}
