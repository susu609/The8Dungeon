package net.ss.sudungeon.client.model;

// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.ss.sudungeon.SsMod;
import net.ss.sudungeon.client.animation.definitions.ModPlayerAnimation;
import net.ss.sudungeon.util.Vars;
import net.ss.sudungeon.world.entity.ModPlayerEntity;
import net.ss.sudungeon.world.entity.player.IPlayerAnim;
import org.jetbrains.annotations.NotNull;

public class ModPlayerModel<T extends Entity> extends ModHierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(SsMod.MODID, "model_mod_player"), "main");

    public final ModelPart bone;
    public final ModelPart body;
    public final ModelPart jacket;
    public final ModelPart head;
    public final ModelPart head_wear;
    public final ModelPart left_arm;
    public final ModelPart left_hand;
    public final ModelPart left_sleeve;
    public final ModelPart left_arm_slim;
    public final ModelPart left_hand_slim;
    public final ModelPart left_sleeve_slim;
    public final ModelPart right_arm;
    public final ModelPart right_hand;
    public final ModelPart right_sleeve;
    public final ModelPart right_arm_slim;
    public final ModelPart right_hand_slim;
    public final ModelPart right_sleeve_slim;
    public final ModelPart left_leg;
    public final ModelPart left_pants;
    public final ModelPart right_leg;
    public final ModelPart right_pants;

    public ModPlayerModel (ModelPart root) {
        super(root);
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.jacket = this.body.getChild("jacket");
        this.head = this.body.getChild("head");
        this.head_wear = this.head.getChild("head_wear");
        this.left_arm = this.body.getChild("left_arm");
        this.left_hand = this.left_arm.getChild("left_hand");
        this.left_sleeve = this.left_arm.getChild("left_sleeve");
        this.left_arm_slim = this.body.getChild("left_arm_slim");
        this.left_hand_slim = this.left_arm_slim.getChild("left_hand_slim");
        this.left_sleeve_slim = this.left_arm_slim.getChild("left_sleeve_slim");
        this.right_arm = this.body.getChild("right_arm");
        this.right_hand = this.right_arm.getChild("right_hand");
        this.right_sleeve = this.right_arm.getChild("right_sleeve");
        this.right_arm_slim = this.body.getChild("right_arm_slim");
        this.right_hand_slim = this.right_arm_slim.getChild("right_hand_slim");
        this.right_sleeve_slim = this.right_arm_slim.getChild("right_sleeve_slim");
        this.left_leg = this.bone.getChild("left_leg");
        this.left_pants = this.left_leg.getChild("left_pants");
        this.right_leg = this.bone.getChild("right_leg");
        this.right_pants = this.right_leg.getChild("right_pants");
    }

    public static LayerDefinition createBodyLayer () {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -11.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 0.0F));

        PartDefinition jacket = body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition head_wear = head.addOrReplaceChild("head_wear", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -9.0F, 0.0F));

        PartDefinition left_hand = left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(2, 1).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 8.5F, -0.5F));

        PartDefinition left_sleeve = left_arm.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_arm_slim = body.addOrReplaceChild("left_arm_slim", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -9.0F, 0.0F));

        PartDefinition left_hand_slim = left_arm_slim.addOrReplaceChild("left_hand_slim", CubeListBuilder.create().texOffs(2, 1).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.5F, -0.5F));

        PartDefinition left_sleeve_slim = left_arm_slim.addOrReplaceChild("left_sleeve_slim", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -9.0F, 0.0F));

        PartDefinition right_hand = right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(19, 30).mirror().addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-0.5F, 8.5F, -0.5F));

        PartDefinition right_sleeve = right_arm.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_arm_slim = body.addOrReplaceChild("right_arm_slim", CubeListBuilder.create().texOffs(41, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -9.0F, 0.0F));

        PartDefinition right_hand_slim = right_arm_slim.addOrReplaceChild("right_hand_slim", CubeListBuilder.create().texOffs(2, 1).mirror().addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 8.5F, -0.5F));

        PartDefinition right_sleeve_slim = right_arm_slim.addOrReplaceChild("right_sleeve_slim", CubeListBuilder.create().texOffs(41, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_leg = bone.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -12.0F, 0.0F));

        PartDefinition left_pants = left_leg.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = bone.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -12.0F, 0.0F));

        PartDefinition right_pants = right_leg.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim (@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof ModPlayerEntity Player)) return;
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Ẩn tay theo slim/wide
        boolean isSlim = Vars.PP(Player).isSlim;
        left_arm.visible = !isSlim;
        right_arm.visible = !isSlim;
        left_arm_slim.visible = isSlim;
        right_arm_slim.visible = isSlim;
        left_hand.visible = !isSlim;
        right_hand.visible = !isSlim;
        left_hand_slim.visible = isSlim;
        right_hand_slim.visible = isSlim;
        left_sleeve.visible = !isSlim;
        right_sleeve.visible = !isSlim;
        left_sleeve_slim.visible = isSlim;
        right_sleeve_slim.visible = isSlim;
        // Phát hoạt ảnh
        if (Player.getAttackAnimationState().isStarted()) {
            AnimationDefinition def = switch (Player.getCurrentAttackAction()) {
                case PLAYER_ATTACK_1 -> ModPlayerAnimation.PLAYER_SWORD_ATTACK1;
                case PLAYER_ATTACK_2 -> ModPlayerAnimation.PLAYER_SWORD_ATTACK2;
                case PLAYER_ATTACK_3 -> ModPlayerAnimation.PLAYER_SWORD_ATTACK3;
                default -> null;
            };
            if (def != null) this.animate(Player.getAttackAnimationState(), def, ageInTicks);
        }

        animate(Player.getHurtAnimationState(), ModPlayerAnimation.PLAYER_HURT, ageInTicks);

        // Nếu không có hoạt ảnh nào thì idle hoặc đi bộ
        if (Player.isWalking()) {
            animateWalk(ModPlayerAnimation.PLAYER_WALK, limbSwing, entity.walkDist, 1.0f, 4.0f);
        } else {
            animate(Player.getIdleAnimationState(), ModPlayerAnimation.PLAYER_IDLE, ageInTicks);
        }
    }

    public void setupAnimForRenderEvent (Player player, float partialTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.bone.yRot = 180;
        float limbSwing = player.walkAnimation.position();
        float limbSwingAmount = player.walkAnimation.speed();
        long tick = (long) (player.tickCount + partialTicks);

        // Hướng di chuyển → hướng nhìn tương đối với góc quay thực tế
        float bodyYaw = Mth.rotLerp(partialTicks, player.yRotO, player.getYRot());

        // Head: chỉ xoay theo hướng di chuyển
        this.head.yRot = 0;
        this.head.xRot = player.getXRot() * ((float) Math.PI / 180F);
        this.head_wear.copyFrom(this.head);

        boolean isSlim = Vars.PP(player).isSlim;
        left_arm.visible = !isSlim;
        right_arm.visible = !isSlim;
        left_arm_slim.visible = isSlim;
        right_arm_slim.visible = isSlim;
        left_hand.visible = !isSlim;
        right_hand.visible = !isSlim;
        left_hand_slim.visible = isSlim;
        right_hand_slim.visible = isSlim;
        left_sleeve.visible = !isSlim;
        right_sleeve.visible = !isSlim;
        left_sleeve_slim.visible = isSlim;
        right_sleeve_slim.visible = isSlim;

        if (!(player instanceof IPlayerAnim anim)) return;

        // Animate theo trạng thái
        // Tính toán các giá trị di chuyển
        boolean isMoving = player.zza != 0 || player.xxa != 0;
        boolean isSprinting = player.isSprinting();

        // Xử lý animation tương ứng
        if (isMoving) {
            if (isSprinting) {
                // Chạy (sprinting)
                animateWalk(ModPlayerAnimation.PLAYER_RUN, limbSwingAmount, player.walkDist, 1.0f, 4.0f);
            } else {
                // Đi bộ
                animateWalk(ModPlayerAnimation.PLAYER_WALK, limbSwingAmount, player.walkDist, 1.0f, 4.0f);
            }
        } else {
            animate(anim.getIdleAnimState(), ModPlayerAnimation.PLAYER_IDLE, partialTicks);
        }
        animate(anim.getHurtAnimState(), ModPlayerAnimation.PLAYER_HURT, partialTicks);
        animate(anim.getAttackAnimState(), ModPlayerAnimation.PLAYER_SWORD_ATTACK1, partialTicks);
        animate(anim.getDrinkAnimState(), ModPlayerAnimation.PLAYER_DRINK, partialTicks);
    }


    public void renderForHand (HumanoidArm arm, PoseStack poseStack, VertexConsumer buffer, int packedLight, float red, float green, float blue, float alpha) {
        // tuỳ chỉnh khi dùng hand
    }


    @Override
    public void renderToBuffer (@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public @NotNull ModelPart root () {
        return this.bone;
    }

    public void setAllVisible (boolean visible) {
        this.bone.visible = visible;

        this.body.visible = visible;
        this.jacket.visible = visible;
        this.head.visible = visible;
        this.head_wear.visible = visible;

        this.left_arm.visible = visible;
        this.left_hand.visible = visible;
        this.left_sleeve.visible = visible;
        this.left_arm_slim.visible = visible;
        this.left_hand_slim.visible = visible;
        this.left_sleeve_slim.visible = visible;

        this.right_arm.visible = visible;
        this.right_hand.visible = visible;
        this.right_sleeve.visible = visible;
        this.right_arm_slim.visible = visible;
        this.right_hand_slim.visible = visible;
        this.right_sleeve_slim.visible = visible;

        this.left_leg.visible = visible;
        this.left_pants.visible = visible;
        this.right_leg.visible = visible;
        this.right_pants.visible = visible;
    }

    private boolean isSlim;

    public void setSlim (boolean slim) {
        this.isSlim = slim;
    }


}