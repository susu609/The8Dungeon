package net.susu609.the8dungeon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.susu609.the8dungeon.client.SpectatorCam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bot đang được camera theo dõi luôn có viền sáng (thấy xuyên khối), chỉ ở client này. */
@Mixin(Minecraft.class)
public abstract class MinecraftGlowMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void susu609$glowFollowTarget(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SpectatorCam.isFollowTarget(entity)) cir.setReturnValue(true);
    }
}
