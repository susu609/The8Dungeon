package net.ss.sudungeon.client.player;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.client.resources.SkinManager; // Giả sử bạn có một lớp SkinManager để quản lý các skin
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    public void getSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation customSkin = SkinManager.getCustomSkin((AbstractClientPlayer) (Object) this);
        if (customSkin != null) {
            cir.setReturnValue(customSkin);
        }
    }
}