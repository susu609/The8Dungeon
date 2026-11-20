package net.susu609.the8dungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.client.model.PedestalModel;
import net.susu609.the8dungeon.world.level.block.entity.PedestalEntity;
import org.jetbrains.annotations.NotNull;

public class PedestalRenderer extends EntityRenderer<PedestalEntity> {

    private final PedestalModel<PedestalEntity> model;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/stone.png");

    public PedestalRenderer(EntityRendererProvider.Context ctx, PedestalModel<PedestalEntity> model) {
        super(ctx);
        this.model = model;
        this.shadowRadius = 0F;
        // TODO (a): "nướng" layer thành ModelPart rồi tạo model
        //           gợi ý: new PedestalModel<>(ctx.bakeLayer(PedestalModel.LAYER_LOCATION))
        new PedestalModel<>(ctx.bakeLayer(PedestalModel.LAYER_LOCATION))
    }

    @Override
    public void render(@NotNull PedestalEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        // TODO (b): lật hệ toạ độ như mob vanilla: pose.scale(...) rồi pose.translate(...)
        // TODO (c): gọi model.setupAnim(entity, 0F, 0F, entity.tickCount + partialTick, 0F, 0F)
        VertexConsumer vc = buffers.getBuffer(model.renderType(getTextureLocation(entity)));
        // TODO (d): model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F)
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PedestalEntity entity) {
        return TEXTURE;
    }
}