package net.ss.sudungeon.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.client.model.ModZombieModel;
import net.ss.sudungeon.world.entity.ModZombieEntity;
import org.jetbrains.annotations.NotNull;

public class ModZombieRenderer extends MobRenderer<ModZombieEntity, ModZombieModel<ModZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/zombie/zombie.png");

    public ModZombieRenderer (EntityRendererProvider.Context context) {
        super(context, new ModZombieModel<>(context.bakeLayer(ModZombieModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation (@NotNull ModZombieEntity p_114482_) {
        return TEXTURE;
    }
}
