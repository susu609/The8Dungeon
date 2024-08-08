/*
package net.ss.sudungeon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;

public class ClassicPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ResourceLocation PLAYER_SKIN;

    public ClassicPlayerRenderer(EntityRendererProvider.Context context, boolean useSmallArms, ResourceLocation skin) {
        super(context, new PlayerModel<>(context.bakeLayer(PlayerModel.createMesh(useSmallArms)), useSmallArms), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(context.bakeLayer(useSmallArms ? ModelLayers.PLAYER_ARMOR_OUTER : ModelLayers.PLAYER_ARMOR_INNER)), new HumanoidArmorModel<>(context.bakeLayer(useSmallArms ? ModelLayers.PLAYER_ARMOR_OUTER : ModelLayers.PLAYER_ARMOR_INNER))));
        this.addLayer(new PlayerItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new Deadmau5EarsLayer<>(this));
        this.addLayer(new CapeLayer<>(this));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new ParrotOnShoulderLayer<>(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this));
        this.PLAYER_SKIN = skin;
    }

    @Override
    public void render(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(AbstractClientPlayer entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer entity, float partialTicks) {
        return entity.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(entity, partialTicks);
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer entity, Component name, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.renderNameTag(entity, name, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return PLAYER_SKIN;
    }
}
*/
