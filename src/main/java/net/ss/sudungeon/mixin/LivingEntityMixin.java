package net.ss.sudungeon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "checkFallDamage", at = @At("HEAD"), cancellable = true)
    private void onVoidDamage(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self.level().dimension().location().equals(new ResourceLocation("ss", "dungeon_dimension"))
                && self.getY() < -64) {
            self.setPos(self.getX(), 77, self.getZ()); // Dịch lên lại
            self.setDeltaMovement(Vec3.ZERO);
            ci.cancel(); // Ngăn xử lý damage
        }
    }
}
