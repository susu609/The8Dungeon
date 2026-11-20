package net.susu609.the8dungeon.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.susu609.the8dungeon.world.level.block.entity.PedestalEntity;
import org.jetbrains.annotations.NotNull;

public class PedestalModel<T extends PedestalEntity> extends HierarchicalModel<T> {

    private final ModelPart root;

    public PedestalModel(ModelPart part) {
        this.root = part.getChild("root");

    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("root", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7F, -3F, -7F, 14F, 3F, 14F)    // base
                        .texOffs(0, 0).addBox(-4F, -13F, -4F, 8F, 10F, 8F)    // column
                        .texOffs(0, 0).addBox(-6F, -16F, -6F, 12F, 3F, 12F),  // top
                PartPose.offset(0F, 24F, 0F));
        return LayerDefinition.create(mesh, 16, 16);   // texture 16x16
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);   // bệ đứng yên
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}