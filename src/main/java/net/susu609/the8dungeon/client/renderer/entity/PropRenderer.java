package net.susu609.the8dungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.data.ModelData;
import net.susu609.the8dungeon.world.level.block.entity.PropEntity;
import org.jetbrains.annotations.NotNull;

public class PropRenderer extends EntityRenderer<PropEntity> {
    private final BlockRenderDispatcher blocks;

    public PropRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.blocks = ctx.getBlockRenderDispatcher();
        // TODO: shadowRadius = 0
        this.shadowRadius = 0;
    }

    @Override
    public void render(PropEntity prop, float yaw, float partialTick, PoseStack pose, @NotNull MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(-0.5, 0, -0.5);
        this.blocks.renderSingleBlock(prop.getBlockState(), pose, buffers, light,
                OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.debugLineStrip(OverlayTexture.NO_OVERLAY));
        pose.popPose();
        super.render(prop, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PropEntity prop) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}