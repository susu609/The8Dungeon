package net.ss.sudungeon.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.model.TargetDummyModel;
import net.ss.sudungeon.world.entity.TargetDummy;
import org.jetbrains.annotations.NotNull;

public class TargetDummyRenderer extends MobRenderer<TargetDummy, TargetDummyModel<TargetDummy>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SsMod.MOD_ID, "textures/entity/target_dummy.png");

    public TargetDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new TargetDummyModel<>(context.bakeLayer(TargetDummyModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TargetDummy entity) {
        return TEXTURE;  // Trả về texture của thực thể
    }

}