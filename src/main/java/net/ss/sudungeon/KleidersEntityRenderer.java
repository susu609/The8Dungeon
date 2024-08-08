/*
package net.ss.sudungeon;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@OnlyIn(Dist.CLIENT)
public class KleidersEntityRenderer extends MobRenderer {
    private final ResourceLocation PLAYER_SKIN;
    public EntityModel MODEL;

    public KleidersEntityRenderer(EntityRendererProvider.Context context, ResourceLocation skin, EntityModel model) {
        this(context, skin);
        this.MODEL = model;
        this.setModel(model);
    }

    public void setModel(EntityModel model) {
        ObfuscationReflectionHelper.setPrivateValue(LivingEntityRenderer.class, this, model, "f_115290_");
    }

    @Nullable
    protected RenderType m_7225_(LivingEntity p_115322_, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        ResourceLocation resourcelocation = this.m_5478_(p_115322_);
        if (p_115324_) {
            return RenderType.m_110467_(resourcelocation);
        } else if (p_115323_) {
            return RenderType.m_110470_(resourcelocation);
        } else {
            return p_115325_ ? RenderType.m_110491_(resourcelocation) : null;
        }
    }

    public boolean m_5523_(Mob livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        if (super.m_5523_(livingEntityIn, camera, camX, camY, camZ)) {
            return true;
        } else {
            Entity entity = livingEntityIn.m_21524_();
            return false;
        }
    }

    public KleidersEntityRenderer(EntityRendererProvider.Context context, ResourceLocation skin) {
        super(context, new PigModel(context.m_174023_(ModelLayers.f_171205_)), 0.5F);
        this.PLAYER_SKIN = skin;
    }

    public ResourceLocation m_5478_(Entity entity) {
        return this.PLAYER_SKIN;
    }

    public void m_7392_(Mob entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        try {
            super.m_7392_(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        } catch (Exception var8) {
            matrixStackIn.m_85849_();
        }

    }

    public ResourceLocation getTextureLocation(Mob entity) {
        return this.PLAYER_SKIN;
    }

    @Override
    public ResourceLocation getTextureLocation (Entity p_114482_) {
        return null;
    }
}
*/
