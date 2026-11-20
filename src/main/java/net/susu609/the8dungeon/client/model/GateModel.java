package net.susu609.the8dungeon.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.susu609.the8dungeon.world.level.block.entity.GateEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Model "GateModel" sinh bởi plugin Blockbench java_single_texture, theo mẫu vanilla (như FrogModel).
 * Đừng sửa tay: sửa trong Blockbench rồi xuất lại.
 *
 * Đăng ký layer (ModEntityRenderersRegistry, sự kiện RegisterLayerDefinitions):
 *   event.registerLayerDefinition(GateModel.LAYER_LOCATION, GateModel::createBodyLayer);
 *
 * Trong renderer (EntityRenderer thường, ví dụ PropRenderer):
 *   model = new GateModel<>(ctx.bakeLayer(GateModel.LAYER_LOCATION));
 *   pose.pushPose();
 *   pose.scale(-1F, -1F, 1F);          // model entity có trục Y hướng xuống -> lật lại
 *   pose.translate(0F, -1.501F, 0F);   // root ở y = 24 rơi đúng mặt đất
 *   model.renderToBuffer(pose, buffers.getBuffer(model.renderType(GateModel.TEXTURE)),
 *           light, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
 *   pose.popPose();
 */
public class GateModel<T extends GateEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("susu609", "gate"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/stone.png");

    private final ModelPart root;
    private final ModelPart gate;

    public GateModel(ModelPart part) {
        super(RenderType::entityCutoutNoCull);
        this.root = part.getChild("root");
        this.gate = this.root.getChild("gate");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        PartDefinition root = parts.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("gate", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-16.0F, -48.0F, -8.0F, 32.0F, 48.0F, 16.0F) /* gate */, PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);   // đứng yên: về tư thế gốc
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

}
