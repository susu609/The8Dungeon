/*
package net.ss.sudungeon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExternalPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ResourceLocation PLAYER_SKIN;

    public ExternalPlayerRenderer(EntityRendererProvider.Context context, ResourceLocation skin) {
        this(context, false, skin);
    }

    public ExternalPlayerRenderer(EntityRendererProvider.Context context, boolean useSmallArms, ResourceLocation skin) {
        super(context, new PlayerModel(context.m_174023_(ModelLayers.f_171162_), useSmallArms), 0.5F);
        this.m_115326_(new HumanoidArmorLayer(this, new HumanoidArmorModel(context.m_174023_(useSmallArms ? ModelLayers.f_171167_ : ModelLayers.f_171164_)), new HumanoidArmorModel(context.m_174023_(useSmallArms ? ModelLayers.f_171168_ : ModelLayers.f_171165_)), context.m_266367_()));
        this.m_115326_(new PlayerItemInHandLayer(this, context.m_234598_()));
        this.m_115326_(new ArrowLayer(context, this));
        this.m_115326_(new Deadmau5EarsLayer(this));
        this.m_115326_(new CapeLayer(this));
        this.m_115326_(new CustomHeadLayer(this, context.m_174027_(), context.m_234598_()));
        this.m_115326_(new ElytraLayer(this, context.m_174027_()));
        this.m_115326_(new ParrotOnShoulderLayer(this, context.m_174027_()));
        this.m_115326_(new SpinAttackEffectLayer(this, context.m_174027_()));
        this.m_115326_(new BeeStingerLayer(this));
        this.PLAYER_SKIN = skin;
    }

    public void render(AbstractClientPlayer entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        this.setModelProperties(entityIn);
        super.m_7392_(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    public Vec3 getRenderOffset(AbstractClientPlayer entityIn, float partialTicks) {
        return entityIn.m_6047_() ? new Vec3(0.0, -0.125, 0.0) : super.m_7860_(entityIn, partialTicks);
    }

    private void setModelProperties(AbstractClientPlayer clientPlayer) {
        PlayerModel<AbstractClientPlayer> playermodel = (PlayerModel)this.m_7200_();
        if (clientPlayer.m_5833_()) {
            playermodel.m_8009_(false);
            playermodel.f_102808_.f_104207_ = true;
            playermodel.f_102809_.f_104207_ = true;
        } else {
            playermodel.m_8009_(true);
            playermodel.f_102808_.f_104207_ = false;
            playermodel.f_102810_.f_104207_ = false;
            playermodel.f_102814_.f_104207_ = false;
            playermodel.f_102813_.f_104207_ = false;
            playermodel.f_102812_.f_104207_ = false;
            playermodel.f_102811_.f_104207_ = false;
            playermodel.f_102809_.f_104207_ = clientPlayer.m_36170_(PlayerModelPart.HAT);
            playermodel.f_103378_.f_104207_ = clientPlayer.m_36170_(PlayerModelPart.JACKET);
            playermodel.f_103376_.f_104207_ = clientPlayer.m_36170_(PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.f_103377_.f_104207_ = clientPlayer.m_36170_(PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.f_103374_.f_104207_ = clientPlayer.m_36170_(PlayerModelPart.LEFT_SLEEVE);
            playermodel.f_103375_.f_104207_ = clientPlayer.m_36170_(PlayerModelPart.RIGHT_SLEEVE);
            playermodel.f_102817_ = clientPlayer.m_6047_();
            HumanoidModel.ArmPose bipedmodel$armpose = getArmPose(clientPlayer, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose bipedmodel$armpose1 = getArmPose(clientPlayer, InteractionHand.OFF_HAND);
            if (bipedmodel$armpose.m_102897_()) {
                bipedmodel$armpose1 = clientPlayer.m_21206_().m_41619_() ? ArmPose.EMPTY : ArmPose.ITEM;
            }

            if (clientPlayer.m_5737_() == HumanoidArm.RIGHT) {
                playermodel.f_102816_ = bipedmodel$armpose;
                playermodel.f_102815_ = bipedmodel$armpose1;
            } else {
                playermodel.f_102816_ = bipedmodel$armpose1;
                playermodel.f_102815_ = bipedmodel$armpose;
            }
        }

    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer p_241741_0_, InteractionHand p_241741_1_) {
        ItemStack itemstack = p_241741_0_.m_21120_(p_241741_1_);
        if (itemstack.m_41619_()) {
            return ArmPose.EMPTY;
        } else {
            if (p_241741_0_.m_7655_() == p_241741_1_ && p_241741_0_.m_21212_() > 0) {
                UseAnim useaction = itemstack.m_41780_();
                if (useaction == UseAnim.BLOCK) {
                    return ArmPose.BLOCK;
                }

                if (useaction == UseAnim.BOW) {
                    return ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAnim.SPEAR) {
                    return ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAnim.CROSSBOW && p_241741_1_ == p_241741_0_.m_7655_()) {
                    return ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!p_241741_0_.f_20911_ && itemstack.m_41720_() == Items.f_42717_ && CrossbowItem.m_40932_(itemstack)) {
                return ArmPose.CROSSBOW_HOLD;
            }

            return ArmPose.ITEM;
        }
    }

    public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return this.PLAYER_SKIN;
    }

    protected void scale(AbstractClientPlayer entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        float f = 0.9375F;
        matrixStackIn.m_85841_(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderNameTag(AbstractClientPlayer entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        this.f_114476_.m_114471_(entityIn);
        matrixStackIn.m_85836_();
        super.m_7649_(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
        matrixStackIn.m_85849_();
    }

    public void renderRightHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn) {
        this.renderHand(matrixStackIn, bufferIn, combinedLightIn, playerIn, ((PlayerModel)this.f_115290_).f_102811_, ((PlayerModel)this.f_115290_).f_103375_);
    }

    public void renderLeftHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn) {
        this.renderHand(matrixStackIn, bufferIn, combinedLightIn, playerIn, ((PlayerModel)this.f_115290_).f_102812_, ((PlayerModel)this.f_115290_).f_103374_);
    }

    private void renderHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
        PlayerModel<AbstractClientPlayer> playermodel = (PlayerModel)this.m_7200_();
        this.setModelProperties(playerIn);
        playermodel.f_102608_ = 0.0F;
        playermodel.f_102817_ = false;
        playermodel.f_102818_ = 0.0F;
        playermodel.m_6973_(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rendererArmIn.f_104203_ = 0.0F;
        rendererArmIn.m_104301_(matrixStackIn, bufferIn.m_6299_(RenderType.m_110446_(playerIn.m_108560_())), combinedLightIn, OverlayTexture.f_118083_);
        rendererArmwearIn.f_104203_ = 0.0F;
        rendererArmwearIn.m_104301_(matrixStackIn, bufferIn.m_6299_(RenderType.m_110473_(playerIn.m_108560_())), combinedLightIn, OverlayTexture.f_118083_);
    }

    protected void setupRotations(AbstractClientPlayer entityLiving, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        float f = entityLiving.m_20998_(partialTicks);
        float f3;
        float f2;
        if (entityLiving.m_21255_()) {
            super.m_7523_(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
            f3 = (float)entityLiving.m_21256_() + partialTicks;
            f2 = Mth.m_14036_(f3 * f3 / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.m_21209_()) {
                matrixStackIn.m_252781_(Axis.f_252529_.m_252977_(f2 * (-90.0F - entityLiving.m_146909_())));
            }

            Vec3 vector3d = entityLiving.m_20252_(partialTicks);
            Vec3 vector3d1 = entityLiving.m_20184_();
            double d0 = vector3d1.m_165925_();
            double d1 = vector3d.m_165925_();
            if (d0 > 0.0 && d1 > 0.0) {
                double d2 = (vector3d1.f_82479_ * vector3d.f_82479_ + vector3d1.f_82481_ * vector3d.f_82481_) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.f_82479_ * vector3d.f_82481_ - vector3d1.f_82481_ * vector3d.f_82479_;
                matrixStackIn.m_252781_(Axis.f_252436_.m_252961_((float)(Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            super.m_7523_(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
            f3 = entityLiving.m_20069_() ? -90.0F - entityLiving.m_146909_() : -90.0F;
            f2 = Mth.m_14179_(f, 0.0F, f3);
            matrixStackIn.m_252781_(Axis.f_252529_.m_252977_(f2));
            if (entityLiving.m_6067_()) {
                matrixStackIn.m_85837_(0.0, -1.0, 0.30000001192092896);
            }
        } else {
            super.m_7523_(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
        }

    }
}
*/
