package com.heytozzz.htzcars.client.model;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Temporary go-kart model, used by every vehicle class until the final models are in.
 * Adapted from the Blockbench export (5.2.2): the part names are kept, and the pivots of the two front wheels were moved
 * to the center of each wheel (the geometry is identical). Client-only code.
 */
public class GokartModel extends EntityModel<VehicleEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HtzCars.MOD_ID, "gokart"), "main");

    private final ModelPart root;

    public GokartModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 78).addBox(-1.25F, -8.5F, -14.325F, 2.0F, 4.0F, 2.0F, CubeDeformation.NONE), PartPose.offset(0.25F, 25.125F, 0.3741F));

        body.addOrReplaceChild("steering_wheel_axis_r1", CubeListBuilder.create().texOffs(72, 1).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 6.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(-0.25F, -7.9588F, -13.0184F, 0.3927F, 0.0F, 0.0F));

        PartDefinition chasis = body.addOrReplaceChild("chasis", CubeListBuilder.create().texOffs(31, 44).addBox(-6.75F, -5.0F, 6.5F, 13.0F, 4.0F, 4.0F, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(-4.75F, -2.5F, -18.5F, 9.0F, 1.0F, 26.0F, CubeDeformation.NONE)
                .texOffs(58, 53).addBox(-6.75F, -11.5F, 10.25F, 13.0F, 2.0F, 3.0F, CubeDeformation.NONE)
                .texOffs(0, 39).addBox(4.25F, -3.0F, -9.5F, 2.0F, 2.0F, 13.0F, CubeDeformation.NONE)
                .texOffs(37, 28).addBox(-6.75F, -3.0F, -9.5F, 2.0F, 2.0F, 13.0F, CubeDeformation.NONE), PartPose.offset(0.0F, -2.0F, -0.25F));

        chasis.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(71, 10).addBox(6.25F, -1.75F, -0.5F, 9.0F, 2.0F, 2.0F, CubeDeformation.NONE)
                .texOffs(11, 72).addBox(4.25F, -1.75F, -0.5F, 2.0F, 2.0F, 3.0F, CubeDeformation.NONE)
                .texOffs(0, 72).addBox(15.25F, -1.75F, -0.5F, 2.0F, 2.0F, 3.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(-11.0F, -2.9445F, -20.091F, -0.7854F, 0.0F, 0.0F));

        chasis.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(46, 70).addBox(-1.75F, -5.875F, -1.25F, 3.0F, 9.0F, 2.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(0.0F, -4.375F, 10.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition seat = body.addOrReplaceChild("seat", CubeListBuilder.create().texOffs(0, 28).addBox(-5.0F, 0.378F, -6.9305F, 10.0F, 2.0F, 8.0F, CubeDeformation.NONE), PartPose.offset(-0.25F, -6.878F, -0.0695F));

        seat.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(58, 59).addBox(-4.0F, -6.0F, 1.0F, 8.0F, 8.0F, 3.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(0.0F, -0.622F, -0.1805F, -0.4363F, 0.0F, 0.0F));

        body.addOrReplaceChild("wheel_bl", CubeListBuilder.create().texOffs(31, 53).addBox(-2.5F, -4.0F, -4.0F, 5.0F, 8.0F, 8.0F, CubeDeformation.NONE)
                .texOffs(71, 15).addBox(1.75F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(6.5F, -5.125F, 8.0F));

        body.addOrReplaceChild("wheel_br", CubeListBuilder.create().texOffs(0, 55).addBox(-2.5F, -4.0F, -4.0F, 5.0F, 8.0F, 8.0F, CubeDeformation.NONE)
                .texOffs(57, 71).addBox(-2.75F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(-7.0F, -5.125F, 8.0F));

        // Front wheels: pivot moved to the center of the wheel (export had it on the rear edge).
        body.addOrReplaceChild("wheel_fr", CubeListBuilder.create().texOffs(68, 28).addBox(-1.5F, -3.0F, -3.0F, 3.0F, 6.0F, 6.0F, CubeDeformation.NONE)
                .texOffs(0, 78).addBox(-1.75F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, CubeDeformation.NONE), PartPose.offset(-6.0F, -4.125F, -13.5F));

        body.addOrReplaceChild("wheel_fl", CubeListBuilder.create().texOffs(27, 70).addBox(-1.5F, -3.0F, -3.0F, 3.0F, 6.0F, 6.0F, CubeDeformation.NONE)
                .texOffs(9, 78).addBox(0.75F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, CubeDeformation.NONE), PartPose.offset(5.5F, -4.125F, -13.5F));

        body.addOrReplaceChild("shaft_f", CubeListBuilder.create().texOffs(66, 44).addBox(-5.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, CubeDeformation.NONE), PartPose.offset(-0.25F, -4.125F, -13.3741F));

        PartDefinition steeringWheel = body.addOrReplaceChild("steering_wheel", CubeListBuilder.create(), PartPose.offset(-0.25F, -10.1837F, -7.8361F));

        steeringWheel.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(68, 71).addBox(-2.5F, -2.5F, -1.0F, 5.0F, 5.0F, 1.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(0.0F, -0.1913F, 0.3619F, 0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(VehicleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // No animation yet.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, consumer, packedLight, packedOverlay, color);
    }
}
