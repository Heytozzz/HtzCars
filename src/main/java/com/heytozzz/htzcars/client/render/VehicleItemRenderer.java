package com.heytozzz.htzcars.client.render;

import com.heytozzz.htzcars.client.model.GokartModel;
import com.heytozzz.htzcars.registry.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the vehicle items (inventory, hand, ground...) as the 3D model with the color stored in the stack. Client-only code. */
public class VehicleItemRenderer extends BlockEntityWithoutLevelRenderer {

    /** Initial values, to be tuned by looking at the result in game. */
    private static final float ITEM_SCALE = 0.45F;
    private static final float ITEM_OFFSET_Y = 0.3F;

    private final EntityModelSet modelSet;
    private GokartModel model;

    public VehicleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.modelSet = modelSet;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        super.onResourceManagerReload(resourceManager);
        // The model is baked again the next time it is needed.
        this.model = null;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            model = new GokartModel(modelSet.bakeLayer(GokartModel.LAYER_LOCATION));
        }
        DyeColor color = stack.getOrDefault(ModDataComponents.VEHICLE_COLOR.get(), DyeColor.WHITE);

        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_OFFSET_Y, 0.5F);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        VehicleRenderer.renderModel(model, poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
