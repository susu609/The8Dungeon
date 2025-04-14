package net.ss.sudungeon.mixin;

import net.minecraft.client.Camera;
import net.ss.sudungeon.world.entity.player.CameraAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Camera.class)
public abstract class CameraAccessorMixin implements CameraAccess {
    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Override
    public void ss_setPosition(double x, double y, double z) {
        this.setPosition(x, y, z);
    }

    @Override
    public void ss_setRotation(float yRot, float xRot) {
        this.setRotation(yRot, xRot);
    }
}
