package net.susu609.the8dungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.client.model.PedestalModel;
import net.susu609.the8dungeon.world.level.block.entity.PedestalEntity;
import org.jetbrains.annotations.NotNull;

public class PedestalRenderer extends EntityRenderer<PedestalEntity> {

    private final PedestalModel<PedestalEntity> model;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/stone.png");

    public PedestalRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0F;
        this.model = new PedestalModel<>(ctx.bakeLayer(PedestalModel.LAYER_LOCATION));   // (a)
    }

    @Override
    public void render(@NotNull PedestalEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.scale(-1F, -1F, 1F);                                                         // (b)
        pose.translate(0F, -1.501F, 0F);                                                  // (b)
        model.setupAnim(entity, 0F, 0F, entity.tickCount + partialTick, 0F, 0F);          // (c)
        VertexConsumer vc = buffers.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F); // (d)
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PedestalEntity entity) {
        return TEXTURE;
    }
}