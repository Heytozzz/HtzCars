package com.heytozzz.htzcars.registry;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Creative tab with every vehicle class in the 16 wool colors. Common code. */
public final class ModCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HtzCars.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + HtzCars.MOD_ID + ".main"))
                    .icon(() -> new ItemStack(ModItems.get(VehicleClass.TRAIL)))
                    .displayItems((parameters, output) -> {
                        for (VehicleClass vehicleClass : VehicleClass.values()) {
                            for (DyeColor color : DyeColor.values()) {
                                ItemStack stack = new ItemStack(ModItems.get(vehicleClass));
                                stack.set(ModDataComponents.VEHICLE_COLOR.get(), color);
                                output.accept(stack);
                            }
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
