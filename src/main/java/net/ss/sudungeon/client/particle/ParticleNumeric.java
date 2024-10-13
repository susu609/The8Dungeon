package net.ss.sudungeon.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import static net.ss.sudungeon.event.onLivingHurtEventHandler.latestDamageAmount;

public class ParticleNumeric extends Particle {
    private final int customColor;
    private double number;
    private final int darkColor;
    private float fadeout;
    private static final float SCALE_FACTOR = 0.006F;

    // Constructor với giá trị sát thương lấy từ biến toàn cục latestDamageAmount
    public ParticleNumeric (ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.number = latestDamageAmount;  // Lấy số sát thương từ biến toàn cục
        this.customColor = 0xFFFFFF;
        this.darkColor = (this.customColor & 0x00FFFFFF) | 0x80000000;
        this.lifetime = 35;
        this.fadeout = 1.0F;

        // Giảm vận tốc để hạt bay chậm hơn
        this.yd = vy * 0.5;  // Giảm vận tốc theo trục Y (để hạt bay lên từ từ)
        this.xd = vx * 0.5;  // Giảm vận tốc theo trục X
        this.zd = vz * 0.5;  // Giảm vận tốc theo trục Z
    }

    @Override
    public void render (@NotNull VertexConsumer buffer, Camera camera, float partialTicks) {
        // Giữ nguyên phần render
        PoseStack matrixStack = new PoseStack();
        float rotationYaw = Minecraft.getInstance().player != null ? -Minecraft.getInstance().player.getYRot() : 0.0F;
        float rotationPitch = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getXRot() : 0.0F;
        Vec3 cameraPos = camera.getPosition();

        float posX = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float posY = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float posZ = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        matrixStack.pushPose();
        matrixStack.translate(posX, posY, posZ);

        // Xoay particle theo camera
        Quaternionf rotation = new Quaternionf().rotationY((float) Math.toRadians(rotationYaw));
        matrixStack.mulPose(rotation);
        Quaternionf pitchRotation = new Quaternionf().rotationX((float) Math.toRadians(rotationPitch));
        matrixStack.mulPose(pitchRotation);

        // Điều chỉnh kích thước theo khoảng cách đến camera
        double distanceFromCam = new Vec3(posX, posY, posZ).length();
        float scale = SCALE_FACTOR * (float) distanceFromCam;
        matrixStack.scale(-scale, -scale, scale);  // Scale particle

        // Hiển thị số sát thương
        Font fontRenderer = Minecraft.getInstance().font;

        // Đảm bảo hiển thị cả số nguyên và số thập phân (nếu có)
        // Nếu number là số nguyên, hiển thị dưới dạng số nguyên, nếu là số thập phân thì hiển thị đủ 1 số lẻ
        String numberString = (this.number % 1 == 0)
                ? String.valueOf((int) this.number)   // Hiển thị số nguyên nếu không có phần thập phân
                : String.format("%.1f", this.number); // Hiển thị 1 số lẻ nếu có phần thập phân

        // Hiệu ứng fadeout (alpha)
        float currentAlpha = Mth.clamp(this.fadeout, 0.0F, 1.0F);  // Alpha trong khoảng 0 đến 1
        int colorWithAlpha = this.customColor & 0x00FFFFFF | (int) (currentAlpha * 255) << 24;

        // Tắt depth test và sử dụng blend để hiển thị số lên trên cùng
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Vẽ số sát thương
        MultiBufferSource.BufferSource multiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        fontRenderer.drawInBatch(numberString, -fontRenderer.width(numberString) / 2.0F, 0, colorWithAlpha, false,
                matrixStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        multiBufferSource.endBatch();
        matrixStack.popPose();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Làm hạt di chuyển chậm dần trong mỗi tick (bay từ từ)
        this.xd *= 0.75;  // Giảm vận tốc theo trục X mỗi tick
        this.yd = Math.max(this.yd * 0.95, 0.05);  // Giữ vận tốc Y ở mức dương tối thiểu để đảm bảo không rơi
        this.zd *= 0.75;  // Giảm vận tốc theo trục Z mỗi tick

        // Di chuyển particle
        this.move(this.xd, this.yd, this.zd);

        // Giảm alpha dần dựa trên tuổi của hạt
        this.fadeout = 1.0F - ((float) this.age / this.lifetime);  // Tính fadeout dựa trên tuổi

        // Xóa particle khi hết tuổi thọ
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }



    @Override
    public @NotNull ParticleRenderType getRenderType () {
        return ParticleRenderType.CUSTOM;
    }

    public static ParticleProvider<SimpleParticleType> provider (SpriteSet spriteSet) {
        return (particleType, world, x, y, z, vx, vy, vz) -> new ParticleNumeric(world, x, y, z, vx, vy, vz, spriteSet);
    }
}
