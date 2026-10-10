package com.heytozzz.htzcars.registry;

import com.heytozzz.htzcars.HtzCars;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.DyeColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components used by the mod. Common code. */
public final class ModDataComponents {

    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(HtzCars.MOD_ID);

    /** Color of a vehicle item. A stack without this component is meant to be treated as white. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DyeColor>> VEHICLE_COLOR =
            DATA_COMPONENTS.registerComponentType("vehicle_color",
                    builder -> builder.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC));

    private ModDataComponents() {
    }

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
