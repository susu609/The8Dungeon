package net.susu609.the8dungeon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.Susu609Mod;
import net.susu609.the8dungeon.world.entity.ClawdEntity;
import org.jetbrains.annotations.NotNull;

public class ClawdModel<T extends ClawdEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Susu609Mod.MODID, "clawd_agent"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart claws;
    private final ModelPart claw_left;
    private final ModelPart claw_right;
    private final ModelPart legs;
    private final ModelPart leg_1;
    private final ModelPart leg_2;
    private final ModelPart leg_3;
    private final ModelPart leg_4;

    public ClawdModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.claws = this.body.getChild("claws");
        this.claw_left = this.claws.getChild("claw_left");
        this.claw_right = this.claws.getChild("claw_right");
        this.legs = this.root.getChild("legs");
        this.leg_1 = this.legs.getChild("leg_1");
        this.leg_2 = this.legs.getChild("leg_2");
        this.leg_3 = this.legs.getChild("leg_3");
        this.leg_4 = this.legs.getChild("leg_4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.0F, -3.0F, 12.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition claws = body.addOrReplaceChild("claws", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition claw_left = claws.addOrReplaceChild("claw_left", CubeListBuilder.create().texOffs(36, 0).addBox(0.0F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -1.0F, 0.0F));

        PartDefinition claw_right = claws.addOrReplaceChild("claw_right", CubeListBuilder.create().texOffs(44, 0).addBox(-14.0F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -1.0F, 0.0F));

        PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition leg_1 = legs.addOrReplaceChild("leg_1", CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 0.0F, 0.0F));

        PartDefinition leg_2 = legs.addOrReplaceChild("leg_2", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, 0.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 0.0F, 0.0F));

        PartDefinition leg_3 = legs.addOrReplaceChild("leg_3", CubeListBuilder.create().texOffs(12, 13).addBox(-8.0F, 0.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 0.0F, 0.0F));

        PartDefinition leg_4 = legs.addOrReplaceChild("leg_4", CubeListBuilder.create().texOffs(24, 13).addBox(-11.0F, 0.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull ClawdEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }
}