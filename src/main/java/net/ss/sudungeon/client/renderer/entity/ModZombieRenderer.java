package net.ss.sudungeon.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.model.ModelModZombie;
import net.ss.sudungeon.client.model.ModelTargetDummy;
import net.ss.sudungeon.world.entity.ModZombie;
import net.ss.sudungeon.world.entity.TargetDummy;
import org.jetbrains.annotations.NotNull;

public class ModZombieRenderer extends MobRenderer<ModZombie, ModelModZombie<ModZombie>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/zombie/zombie.png");

    public ModZombieRenderer (EntityRendererProvider.Context context) {
        super(context, new ModelModZombie<>(context.bakeLayer(ModelModZombie.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation (@NotNull ModZombie p_114482_) {
        return TEXTURE;
    }
}
