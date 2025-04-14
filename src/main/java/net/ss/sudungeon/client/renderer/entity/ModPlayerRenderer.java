package net.ss.sudungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.client.model.ModPlayerModel;
import net.ss.sudungeon.util.Vars;
import net.ss.sudungeon.world.entity.ModPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class ModPlayerRenderer extends MobRenderer<ModPlayerEntity, ModPlayerModel<ModPlayerEntity>> {

    public ModPlayerRenderer (EntityRendererProvider.Context context) {
        super(context, new ModPlayerModel<>(context.bakeLayer(ModPlayerModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation (@NotNull ModPlayerEntity entity) {
        String character = Vars.PP(entity).character;
        boolean slim = Vars.PP(entity).isSlim;
        String path = slim ? "slim/" : "wide/";
        return new ResourceLocation("ss", "textures/entity/player/" + path + character + ".png");
    }

    @Override
    public RenderType getRenderType (@NotNull ModPlayerEntity entity, boolean showBody, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }

    @Override
    protected void setupRotations (@NotNull ModPlayerEntity entity, @NotNull PoseStack stack, float ageInTicks, float rotationYaw, float partialTick) {
        super.setupRotations(entity, stack, ageInTicks, rotationYaw, partialTick);
    }
}
