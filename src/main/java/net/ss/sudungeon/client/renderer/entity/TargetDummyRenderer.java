package net.ss.sudungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.animation.definitions.TargetDummyAnimation;
import net.ss.sudungeon.client.model.ModelTargetDummy;
import net.ss.sudungeon.world.entity.TargetDummy;
import org.jetbrains.annotations.NotNull;

public class TargetDummyRenderer extends MobRenderer<TargetDummy, ModelTargetDummy<TargetDummy>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(SsMod.MODID, "textures/entity/target_dummy.png");

    public TargetDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTargetDummy<>(context.bakeLayer(ModelTargetDummy.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TargetDummy entity) {
        return TEXTURE;  // Trả về texture của thực thể
    }

}