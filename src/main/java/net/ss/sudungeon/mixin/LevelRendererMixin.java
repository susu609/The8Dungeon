/*
package net.ss.sudungeon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tr7zw.firstperson.FirstPersonModelCore;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class WorldRendererMixin {
    @Shadow
    private RenderBuffers f_109464_;

    public WorldRendererMixin () {
    }

    @Inject(
            method = {"renderLevel"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    ordinal = 0
            )}
    )
    public void render (PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
        if (!camera.m_90594_() && FirstPersonModelCore.instance.getLogicHandler().shouldApplyThirdPerson(false)) {
            Vec3 vec3d = camera.m_90583_();
            MultiBufferSource.BufferSource immediate = this.f_109464_.m_110104_();
            FirstPersonModelCore.instance.setRenderingPlayer(true);
            this.m_109517_(camera.m_90592_(), vec3d.m_7096_(), vec3d.m_7098_(), vec3d.m_7094_(), tickDelta, matrices, immediate);
            FirstPersonModelCore.instance.setRenderingPlayer(false);
        }
    }

    @Shadow
    private void m_109517_ (Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers) {
    }
}
*/
