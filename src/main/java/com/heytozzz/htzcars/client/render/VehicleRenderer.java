package com.heytozzz.htzcars.client.render;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.client.model.GokartModel;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Renders every vehicle class with the temporary go-kart model, tinted with the vehicle color. Client-only code. */
public class VehicleRenderer extends EntityRenderer<VehicleEntity> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HtzCars.MOD_ID, "textures/entity/vehicle/gokart.png");

    private final GokartModel model;

    public VehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GokartModel(context.bakeLayer(GokartModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(VehicleEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(VehicleEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        // Same setup as vanilla entity models: flipped axes, and moved so the model stands on the ground.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, VehicleTint.argb(entity.getColor()));
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
