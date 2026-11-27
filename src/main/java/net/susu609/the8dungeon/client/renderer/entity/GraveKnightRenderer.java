package net.susu609.the8dungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.susu609.the8dungeon.client.model.GraveKnightModel;
import net.susu609.the8dungeon.world.entity.GraveKnight;

/**
 * Vẽ GraveKnight bằng texture zombie, phóng to SCALE.
 * NHỚ đăng ký: event.registerEntityRenderer(ModEntitiesRegistry.GRAVE_KNIGHT.get(), GraveKnightRenderer::new);
 */
public class GraveKnightRenderer extends AbstractZombieRenderer<GraveKnight, GraveKnightModel> {
    public GraveKnightRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
                new GraveKnightModel(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                new GraveKnightModel(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),   // giáp cũng giơ tay
                new GraveKnightModel(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
    }

    @Override
    protected void scale(GraveKnight entity, PoseStack pose, float partialTick) {
        pose.scale(GraveKnight.SCALE, GraveKnight.SCALE, GraveKnight.SCALE);
    }
}
