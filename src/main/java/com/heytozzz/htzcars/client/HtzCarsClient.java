package com.heytozzz.htzcars.client;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.client.model.GokartModel;
import com.heytozzz.htzcars.client.render.VehicleItemRenderer;
import com.heytozzz.htzcars.client.render.VehicleRenderer;
import com.heytozzz.htzcars.registry.ModEntities;
import com.heytozzz.htzcars.registry.ModItems;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Client-only entry point of the mod. Thanks to dist = Dist.CLIENT it is never loaded on a dedicated server,
 * so everything client-related must be registered from here (or from classes only reachable from here).
 */
@Mod(value = HtzCars.MOD_ID, dist = Dist.CLIENT)
public class HtzCarsClient {

    public HtzCarsClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(HtzCarsClient::onRegisterLayerDefinitions);
        modEventBus.addListener(HtzCarsClient::onRegisterRenderers);
        modEventBus.addListener(HtzCarsClient::onRegisterClientExtensions);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GokartModel.LAYER_LOCATION, GokartModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            event.registerEntityRenderer(ModEntities.get(vehicleClass).get(), VehicleRenderer::new);
        }
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            private VehicleItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                // Created on first use, when the render dispatcher and the model set already exist.
                if (renderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    renderer = new VehicleItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        };
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            event.registerItem(extensions, ModItems.get(vehicleClass).get());
        }
    }
}
