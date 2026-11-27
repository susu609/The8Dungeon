package net.susu609.the8dungeon.client.model;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.susu609.the8dungeon.world.entity.GraveKnight;

/** Model zombie, giơ tay cao qua đầu khi đang gồng (báo hiệu dash). */
public class GraveKnightModel extends ZombieModel<GraveKnight> {
    public GraveKnightModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(GraveKnight e, float limb, float limbAmt, float age, float yaw, float pitch) {
        super.setupAnim(e, limb, limbAmt, age, yaw, pitch);       // tay zombie bình thường
        if (e.isCharging()) {
            rightArm.xRot = -2.6F;                                 // ≈ -150°: giơ cao qua đầu
            leftArm.xRot = -2.6F;
            rightArm.zRot = 0.2F;                                  // hơi dang ra
            leftArm.zRot = -0.2F;
        }
    }
}
