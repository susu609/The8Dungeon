package net.ss.sudungeon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.ss.sudungeon.util.Vars;
import org.jetbrains.annotations.NotNull;

public class ModItemInHandLayer<T extends net.minecraft.world.entity.LivingEntity, M extends ModPlayerModel<T>>
        extends ItemInHandLayer<T, M> {
    private final ItemInHandRenderer renderer;

    public ModItemInHandLayer(RenderLayerParent<T, M> parent, ItemInHandRenderer renderer) {
        super(parent, renderer);
        this.renderer = renderer;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight,
                       @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (entity instanceof Player player) {
            ItemStack right = Vars.PP(player).right_hand == null ? ItemStack.EMPTY : Vars.PP(player).right_hand;
            ItemStack left = Vars.PP(player).left_hand == null ? ItemStack.EMPTY : Vars.PP(player).left_hand;

            if (!right.isEmpty()) {
                renderCustomItem(poseStack, bufferSource, packedLight, player, right, HumanoidArm.RIGHT);
            }

            if (!left.isEmpty()) {
                renderCustomItem(poseStack, bufferSource, packedLight, player, left, HumanoidArm.LEFT);
            }
        } else {
            super.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    private void renderCustomItem(PoseStack poseStack, MultiBufferSource buffer, int light, Player player,
                                  ItemStack item, HumanoidArm arm) {

        poseStack.pushPose();

        // Lấy đúng tay từ model
        boolean isSlim = Vars.PP(player).isSlim;
        ModPlayerModel<?> model = this.getParentModel();

        if (arm == HumanoidArm.RIGHT) {
            if (isSlim) model.right_hand_slim.translateAndRotate(poseStack);
            else model.right_hand.translateAndRotate(poseStack);
        } else {
            if (isSlim) model.left_hand_slim.translateAndRotate(poseStack);
            else model.left_hand.translateAndRotate(poseStack);
        }

        renderer.renderItem(
                player, item,
                (arm == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND),
                false, poseStack, buffer, light
        );

        poseStack.popPose();
    }

}
