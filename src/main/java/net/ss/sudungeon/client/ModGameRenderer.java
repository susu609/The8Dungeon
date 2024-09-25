package net.ss.sudungeon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ModGameRenderer extends GameRenderer {
    public ModGameRenderer (Minecraft p_234219_, ItemInHandRenderer p_234220_, ResourceManager p_234221_, RenderBuffers p_234222_) {
        super(p_234219_, p_234220_, p_234221_, p_234222_);
    }
    @Override
    public void renderLevel(float partialTicks, long nanoTime, @NotNull PoseStack poseStack) {
        // Gọi phương thức render mặc định từ lớp cha để giữ lại hầu hết các chức năng gốc
        super.renderLevel(partialTicks, nanoTime, poseStack);

        // Thêm phần render tùy chỉnh của bạn vào đây.
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.getCameraType().isFirstPerson() && mc.player != null) {
            // Ví dụ: Render mô hình người chơi trong góc nhìn thứ nhất.
            renderPlayerBodyInFirstPerson(mc.player, poseStack, mc.renderBuffers().bufferSource(), mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTicks), partialTicks);
        }
    }

    private void renderPlayerBodyInFirstPerson(AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTicks) {
        // Lấy camera từ Minecraft
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        // Dịch chuyển mô hình ra trước camera
        poseStack.pushPose();
        poseStack.translate(0, -1.6, 0);  // Điều chỉnh vị trí mô hình người chơi phía trước camera
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot())); // Xoay theo góc Y của camera
        poseStack.mulPose(Axis.XP.rotationDegrees(-camera.getXRot())); // Xoay theo góc X của camera

        // Gọi phương thức render mô hình của người chơi
        renderPlayerModel(player, poseStack, bufferSource, packedLight, partialTicks);

        poseStack.popPose();
    }

    private void renderPlayerModel(AbstractClientPlayer player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTicks) {
        // Gọi phương thức render mô hình người chơi (bạn có thể tùy chỉnh lớp renderer của mình nếu cần)
        Minecraft mc = Minecraft.getInstance();
        mc.getEntityRenderDispatcher().render(player, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, poseStack, bufferSource, packedLight);
    }


}
