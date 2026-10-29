package com.heytozzz.htzcars.client.render;

import com.heytozzz.htzcars.client.model.VehicleModel;
import com.heytozzz.htzcars.client.model.VehicleModels;
import com.heytozzz.htzcars.registry.ModDataComponents;
import com.heytozzz.htzcars.registry.ModItems;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the vehicle items (inventory, hand, ground...) as the 3D model of their class (the first model of the class)
 * with the color stored in the stack. Client-only code.
 */
public class VehicleItemRenderer extends BlockEntityWithoutLevelRenderer {

    /** Initial values, to be tuned by looking at the result in game. */
    private static final float ITEM_SCALE = 0.45F;
    private static final float ITEM_OFFSET_Y = 0.3F;

    private final EntityModelSet modelSet;
    private final Map<VehicleClass, VehicleModel> models = new EnumMap<>(VehicleClass.class);

    public VehicleItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.modelSet = modelSet;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        super.onResourceManagerReload(resourceManager);
        // The models are baked again the next time they are needed.
        this.models.clear();
    }

    /** The class of the vehicle this stack places, or null if the stack is not a vehicle item. */
    private static VehicleClass classOf(ItemStack stack) {
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            if (stack.getItem() == ModItems.get(vehicleClass).get()) {
                return vehicleClass;
            }
        }
        return null;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VehicleClass vehicleClass = classOf(stack);
        if (vehicleClass == null) {
            return;
        }
        VehicleModel model = models.computeIfAbsent(vehicleClass, key -> {
            VehicleModels.Entry entry = VehicleModels.get(key, 0);
            return new VehicleModel(modelSet.bakeLayer(entry.layerLocation()), entry);
        });
        DyeColor color = stack.getOrDefault(ModDataComponents.VEHICLE_COLOR.get(), DyeColor.WHITE);

        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_OFFSET_Y, 0.5F);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -VehicleModel.MODEL_HEIGHT_OFFSET, 0.0F);
        VehicleRenderer.renderModel(model, poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
