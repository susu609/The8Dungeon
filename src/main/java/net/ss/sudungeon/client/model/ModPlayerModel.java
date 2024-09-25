/*
package net.ss.sudungeon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.ss.sudungeon.network.SsModVariables;
import org.jetbrains.annotations.NotNull;

public class ModPlayerModel extends PlayerModel<Player> {

    // Khởi tạo mô hình với các bộ phận và tùy chọn slim
    public ModPlayerModel (ModelPart modelPart, boolean slim) {
        super(modelPart, slim); // Gọi constructor của PlayerModel
    }

    // Ghi đè phương thức render để thêm các tùy chỉnh
    @Override
    public void renderToBuffer (@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // Logic render tùy chỉnh của bạn cho mô hình người chơi
        super.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    // Phương thức để lấy skin texture tùy chỉnh cho mô hình này
    public ResourceLocation getCustomTexture () {
        // Trả về skin tùy chỉnh cho người chơi
        assert Minecraft.getInstance().player != null;
        SsModVariables.WorldVariables worldVariables = SsModVariables.WorldVariables.get(Minecraft.getInstance().player.level());

        // Kiểm tra nếu biến không null
        if (worldVariables != null) {
            return new ResourceLocation(worldVariables.skinUrl);
        } else {
            // Trả về texture mặc định nếu không có skinUrl
            return new ResourceLocation("minecraft", "textures/entity/steve.png");
        }
    }
}
*/
