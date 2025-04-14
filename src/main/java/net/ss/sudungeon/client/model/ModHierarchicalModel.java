package net.ss.sudungeon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;

public abstract class ModHierarchicalModel<E extends Entity> extends HierarchicalModel<E>
        implements ArmedModel, HeadedModel {

    private final ModelPart rootPart;

    public ModHierarchicalModel(ModelPart rootPart) {
        this.rootPart = rootPart;
    }

    @Override
    public @NotNull ModelPart root() {
        return this.rootPart;
    }

    /** ✅ Override cái này ở model con để chọn tay tương ứng */
    protected @NotNull ModelPart getArm(HumanoidArm arm) {
        // Mặc định không có tay — model con phải override nếu có
        throw new UnsupportedOperationException("Model must override getArm()");
    }

    /** ✅ PlayerItemInHandLayer gọi cái này */
    @Override
    public void translateToHand(@NotNull HumanoidArm arm, @NotNull PoseStack poseStack) {
        getArm(arm).translateAndRotate(poseStack);
    }

    /** ✅ HeadedModel support */
    @Override
    public @NotNull ModelPart getHead() {
        return rootPart.getChild("head"); // Hoặc override nếu custom
    }

    // Giữ lại animation mặc định
    @Override
    public void animate (@NotNull AnimationState state, @NotNull AnimationDefinition def, float tickDelta) {
        super.animate(state, def, tickDelta);
    }

    @Override
    protected void animateWalk(@NotNull AnimationDefinition def, float speed, float walkDistance, float scale, float transition) {
        super.animateWalk(def, speed, walkDistance, scale, transition);
    }

    @Override
    public void setupAnim(@NotNull E entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Có thể để trống nếu model con override toàn bộ
    }

}
