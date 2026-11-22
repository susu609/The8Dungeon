package net.susu609.the8dungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.client.model.GateModel;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;
import org.jetbrains.annotations.NotNull;

public class GateRenderer extends EntityRenderer<GateEntity> {

    private final GateModel<GateEntity> model;

    // Chỉ một tham số Context, để dùng được GateRenderer::new khi đăng ký.
    public GateRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0F;
        this.model = new GateModel<>(ctx.bakeLayer(GateModel.LAYER_LOCATION));            // (a)
    }

    @Override
    public void render(@NotNull GateEntity entity, float yaw, float partialTick,
                       PoseStack pose, @NotNull MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));                                // (e) xoay theo hướng, TRƯỚC khi lật
        pose.scale(-1F, -1F, 1F);                                                         // (b) model entity có Y hướng xuống
        pose.translate(0F, -1.501F, 0F);                                                  // (b) root ở y = 24 rơi đúng mặt đất
        model.setupAnim(entity, 0F, 0F, entity.tickCount + partialTick, 0F, 0F);          // (c)
        VertexConsumer vc = buffers.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F); // (d)
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GateEntity entity) {
        return GateModel.TEXTURE;
    }
}
