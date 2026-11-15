package net.susu609.the8dungeon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.susu609.the8dungeon.client.SpectatorCam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Camera theo dõi bot được đặt ở đúng bước dựng khung hình (không theo tick), dùng vị trí nội suy
 * của bot nên mượt ở mọi FPS. Chỉ có tác dụng khi SpectatorCam đang theo dõi.
 */
@Mixin(Camera.class)
public abstract class CameraFollowMixin {

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void susu609$followBot(BlockGetter level, Entity entity, boolean detached, boolean mirror,
                                    float partialTick, CallbackInfo ci) {
        Vec3 pos = SpectatorCam.cameraPosition(partialTick);
        if (pos == null) return;
        setPosition(pos);
        if (SpectatorCam.lockPitchDown()) setRotation(entity.getViewYRot(partialTick), 90.0F);
    }
}
