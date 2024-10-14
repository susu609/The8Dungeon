/*
package net.ss.sudungeon.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.ss.sudungeon.client.model.ModPlayerModel;
import org.jetbrains.annotations.NotNull;

public class ModPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, ModPlayerModel<AbstractClientPlayer>> {

    // Constructor phù hợp để đảm bảo đúng với lớp cha
    public ModPlayerRenderer(EntityRendererProvider.Context context, boolean useSmallArms) {
        // Gọi constructor lớp cha
        super(context, new ModPlayerModel<>(context.bakeLayer(ModPlayerModel.LAYER_LOCATION)), 0.5F);

        // Thêm các layer tương ứng như PlayerRenderer
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(useSmallArms ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(useSmallArms ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new PlayerItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new Deadmau5EarsLayer(this));
        this.addLayer(new CapeLayer(this));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new ParrotOnShoulderLayer<>(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AbstractClientPlayer entity) {
        return entity.getSkinTextureLocation();
    }

    @Override
    public void render(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.setModelProperties(player);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderPlayerEvent.Pre(player, this, partialTicks, poseStack, bufferSource, packedLight))) {
            return;
        }
        super.render(player, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderPlayerEvent.Post(player, this, partialTicks, poseStack, bufferSource, packedLight));
    }

    private void setModelProperties(AbstractClientPlayer player) {
        ModPlayerModel<AbstractClientPlayer> playerModel = this.getModel();

        if (player.isSpectator()) {
            playerModel.getHead().visible = true;
            playerModel.getHeadwear().visible = true;
            playerModel.getJacket().visible = false;
            playerModel.getLeftPants().visible = false;
            playerModel.getRightPants().visible = false;
            playerModel.getLeftSleeve().visible = false;
            playerModel.getRightSleeve().visible = false;
        } else {
            playerModel.getHead().visible = true;
            playerModel.getHeadwear().visible = player.isModelPartShown(PlayerModelPart.HAT);
            playerModel.getJacket().visible = player.isModelPartShown(PlayerModelPart.JACKET);
            playerModel.getLeftPants().visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playerModel.getRightPants().visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playerModel.getLeftSleeve().visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playerModel.getRightSleeve().visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        }
    }
}
*/
