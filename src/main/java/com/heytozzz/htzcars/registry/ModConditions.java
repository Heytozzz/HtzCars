package com.heytozzz.htzcars.registry;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.condition.VehicleClassEnabledCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Recipe conditions of the mod. Common code. */
public final class ModConditions {

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, HtzCars.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<VehicleClassEnabledCondition>> VEHICLE_CLASS_ENABLED =
            CONDITIONS.register("vehicle_class_enabled", () -> VehicleClassEnabledCondition.CODEC);

    private ModConditions() {
    }

    public static void register(IEventBus modEventBus) {
        CONDITIONS.register(modEventBus);
    }
}
