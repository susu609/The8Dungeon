/*
package net.ss.sudungeon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Final
    @Shadow
    private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void onRenderLevel (float partialTicks, long nanoTime, PoseStack poseStack, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.getCameraType().isFirstPerson() && mc.player != null) {
            // Kiểm tra góc nhìn và chèn logic tùy chỉnh
            System.out.println("First person view detected");

            // Gửi tin nhắn tới người chơi
            mc.player.sendSystemMessage(Component.literal("Bạn đang ở góc nhìn thứ nhất!"));
        }
    }
}
*/
