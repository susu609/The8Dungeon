package net.ss.sudungeon.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ss.sudungeon.client.model.ModZombieModel;
import net.ss.sudungeon.client.model.ZombieBossModel;
import net.ss.sudungeon.world.entity.ModZombieEntity;
import net.ss.sudungeon.world.entity.ZombieBossEntity;
import org.jetbrains.annotations.NotNull;

public class ZombieBossRenderer extends MobRenderer<ZombieBossEntity, ZombieBossModel<ZombieBossEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/zombie/zombie.png");

    public ZombieBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieBossModel<>(context.bakeLayer(ModZombieModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    protected void scale(@NotNull ZombieBossEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.2f, 1.2f, 1.2f); // scale to hơn
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation (@NotNull ZombieBossEntity zombieBossEntity) {
        return TEXTURE;
    }
}

