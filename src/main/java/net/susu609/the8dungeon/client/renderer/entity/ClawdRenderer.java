package net.susu609.the8dungeon.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.client.model.ClawdModel;
import net.susu609.the8dungeon.world.entity.ClawdEntity;
import org.jetbrains.annotations.NotNull;

public class ClawdRenderer extends MobRenderer<ClawdEntity, ClawdModel<ClawdEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "textures/entity/clawd_agent.png");

    public ClawdRenderer(EntityRendererProvider.Context context) {
        super(context, new ClawdModel<>(context.bakeLayer(ClawdModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ClawdEntity entity) {
        return TEXTURE;
    }
}