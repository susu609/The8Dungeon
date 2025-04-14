package net.ss.sudungeon.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.ss.sudungeon.util.MinecraftUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"
            )
    )
    private boolean forceRenderFirstPersonPlayer(EntityRenderDispatcher dispatcher, Entity entity, Frustum frustum, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();

        if (entity instanceof LocalPlayer && mc.options.getCameraType().isFirstPerson()) {
            return true; // Ép phải render
        }

        return dispatcher.shouldRender(entity, frustum, x, y, z);
    }

    // CHẶN luôn phần loại bỏ player khi ở góc nhìn thứ nhất
    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;"
            ),
            require = 2 // Đảm bảo chỉ redirect 1-2 lần
    )
    private Entity bypassCameraEntity(Camera camera) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.getCameraType().isFirstPerson()) {
            return mc.player; // Trả về player thay vì null!
        }

        return camera.getEntity();
    }

}
