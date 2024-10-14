package net.ss.sudungeon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ModPlayerModel<T extends LivingEntity> extends HierarchicalModel<T> {
    // Model layer location
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "modplayer"), "main");

    private final ModelPart bone;
    private final ModelPart body;
    private final ModelPart jacket;
    private final ModelPart head;
    private final ModelPart headwear;
    private final ModelPart left_arm;
    private final ModelPart left_sleeve;
    private final ModelPart right_arm;
    private final ModelPart right_sleeve;
    private final ModelPart left_leg;
    private final ModelPart left_pants;
    private final ModelPart right_leg;
    private final ModelPart right_pants;

    public ModPlayerModel (ModelPart root) {
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.jacket = this.body.getChild("jacket");
        this.head = this.body.getChild("head");
        this.headwear = this.head.getChild("headwear");
        this.left_arm = this.body.getChild("left_arm");
        this.left_sleeve = this.left_arm.getChild("left_sleeve");
        this.right_arm = this.body.getChild("right_arm");
        this.right_sleeve = this.right_arm.getChild("right_sleeve");
        this.left_leg = this.body.getChild("left_leg");
        this.left_pants = this.left_leg.getChild("left_pants");
        this.right_leg = this.body.getChild("right_leg");
        this.right_pants = this.right_leg.getChild("right_pants");
    }

    public static LayerDefinition createBodyLayer () {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition jacket = body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition headwear = head.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition left_sleeve = left_arm.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition right_sleeve = right_arm.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.0F, 12.0F, 0.0F));
        PartDefinition left_pants = left_leg.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
        PartDefinition right_pants = right_leg.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim (@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Đặt lại vị trí ban đầu của tất cả các bộ phận trước khi thêm hoạt ảnh mới
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Chỉ thực hiện hoạt ảnh đi bộ nếu nhân vật đang đứng trên mặt đất
        if (entity.onGround()) {
            this.animateWalk(limbSwing, limbSwingAmount);
        }

        // Hoạt ảnh cho đầu, dù nhân vật di chuyển hay đứng yên
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }


    private void animateWalk (float limbSwing, float limbSwingAmount) {
        // Sử dụng Math.sin() để tạo ra hoạt ảnh mượt mà cho các cánh tay và chân
        float swing = (float) (Math.sin(limbSwing * 0.6662F) * limbSwingAmount);

        // Điều khiển chuyển động tay trái và phải theo sóng
        this.left_arm.xRot = swing * 1.4F;  // Nhân hệ số để tăng độ biên độ chuyển động
        this.right_arm.xRot = swing * -1.4F;

        // Điều khiển chuyển động chân trái và phải
        this.left_leg.xRot = swing * -1.4F;
        this.right_leg.xRot = swing * 1.4F;
    }


    @Override
    public void renderToBuffer (@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public @NotNull ModelPart root () {
        return bone;
    }

    // Thêm getter cho các thuộc tính cần truy cập từ lớp ModPlayerRenderer
    public ModelPart getHead() {
        return head;
    }

    public ModelPart getHeadwear() {
        return headwear;
    }

    public ModelPart getJacket() {
        return jacket;
    }

    public ModelPart getLeftPants() {
        return left_pants;
    }

    public ModelPart getRightPants() {
        return right_pants;
    }

    public ModelPart getLeftSleeve() {
        return left_sleeve;
    }

    public ModelPart getRightSleeve() {
        return right_sleeve;
    }
}
