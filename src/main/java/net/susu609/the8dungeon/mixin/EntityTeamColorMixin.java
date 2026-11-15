package net.susu609.the8dungeon.mixin;

import net.minecraft.world.entity.Entity;
import net.susu609.the8dungeon.client.SpectatorCam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Màu viền (glowing outline) riêng cho từng bot khi xem ở chế độ Người xem. Chỉ ở client. */
@Mixin(Entity.class)
public abstract class EntityTeamColorMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void susu609$botColor(CallbackInfoReturnable<Integer> cir) {
        int c = SpectatorCam.outlineColor((Entity) (Object) this);
        if (c >= 0) cir.setReturnValue(c);
    }
}
