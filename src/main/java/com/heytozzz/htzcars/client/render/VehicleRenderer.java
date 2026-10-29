package com.heytozzz.htzcars.client.render;

import com.heytozzz.htzcars.client.model.VehicleModel;
import com.heytozzz.htzcars.client.model.VehicleModels;
import com.heytozzz.htzcars.vehicle.VehicleClass;
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
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the vehicles of one class with the model that matches the model index of each vehicle (see VehicleModels),
 * tinted with the vehicle color. Client-only code.
 */
public class VehicleRenderer extends EntityRenderer<VehicleEntity> {

    private static final int NO_TINT = 0xFFFFFFFF;

    /** The baked models of the class, in the order of their index. */
    private final List<VehicleModel> models = new ArrayList<>();

    public VehicleRenderer(EntityRendererProvider.Context context, VehicleClass vehicleClass) {
        super(context);
        for (VehicleModels.Entry entry : VehicleModels.of(vehicleClass)) {
            models.add(new VehicleModel(context.bakeLayer(entry.layerLocation()), entry));
        }
    }

    /** The model of this vehicle; if its index does not exist (a model was removed) the first one is used. */
    private VehicleModel modelOf(VehicleEntity entity) {
        int index = entity.getModelIndex();
        return index >= 0 && index < models.size() ? models.get(index) : models.get(0);
    }

    @Override
    public ResourceLocation getTextureLocation(VehicleEntity entity) {
        return modelOf(entity).getTexture();
    }

    @Override
    public void render(VehicleEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        VehicleModel model = modelOf(entity);
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        // Lean while sliding, around the front-to-back axis at ground level (negative angle = lean to the right).
        poseStack.mulPose(Axis.ZP.rotationDegrees(-entity.getVisualTilt(partialTick)));
        // Same setup as vanilla entity models: flipped axes, and moved so the model stands on the ground.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -VehicleModel.MODEL_HEIGHT_OFFSET, 0.0F);
        model.setAngles(entity.getRolledDistance(partialTick), entity.getVisualSteer(partialTick));
        renderModel(model, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, entity.getColor());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /** Draws the tinted base texture and then the untinted details on top. Shared with the item renderer. */
    public static void renderModel(VehicleModel model, PoseStack poseStack, MultiBufferSource buffer,
                                   int packedLight, int packedOverlay, DyeColor color) {
        VertexConsumer base = buffer.getBuffer(model.renderType(model.getTexture()));
        model.renderToBuffer(poseStack, base, packedLight, packedOverlay, VehicleTint.argb(color));
        VertexConsumer details = buffer.getBuffer(model.renderType(model.getDetailsTexture()));
        model.renderToBuffer(poseStack, details, packedLight, packedOverlay, NO_TINT);
    }
}
