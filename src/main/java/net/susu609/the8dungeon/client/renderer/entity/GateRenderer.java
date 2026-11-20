package net.susu609.the8dungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.client.model.GateModel;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;
import org.jetbrains.annotations.NotNull;
public class GateRenderer extends EntityRenderer<GateEntity> {

    private final GateModel<GateEntity> model;

    public GateRenderer(EntityRendererProvider.Context ctx,
            GateModel<GateEntity> model) {
        super(ctx);
        this.model = model;
        this.shadowRadius = 0F;
        // TODO (a): tạo model từ GateModel.LAYER_LOCATION (giống bệ đỡ)
    }

    @Override
    public void render(@NotNull GateEntity entity, float yaw, float partialTick,
            PoseStack pose, @NotNull MultiBufferSource buffers, int light) {
        pose.pushPose();
        // TODO (e) MỚI: xoay quanh trục Y theo hướng entity, TRƯỚC khi lật
        //          gợi ý: pose.mulPose(Axis.YP.rotationDegrees(180F - yaw));
        // TODO (b): lật hệ toạ độ (2 dòng scale + translate như bệ đỡ)
        // TODO (c): setupAnim
        // TODO (d): lấy VertexConsumer và renderToBuffer
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GateEntity entity) {
        return GateModel.TEXTURE;
    }
}