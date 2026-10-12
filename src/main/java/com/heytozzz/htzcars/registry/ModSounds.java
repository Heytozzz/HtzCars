package com.heytozzz.htzcars.registry;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events: one engine sound per vehicle class (htzcars:vehicle.<class>.engine). The files they play are set in
 * assets/htzcars/sounds.json, so each class can get its own sound just by editing that file. Common code.
 */
public final class ModSounds {

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, HtzCars.MOD_ID);

    private static final Map<VehicleClass, DeferredHolder<SoundEvent, SoundEvent>> ENGINES = new EnumMap<>(VehicleClass.class);

    static {
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            String name = "vehicle." + vehicleClass.getId() + ".engine";
            ENGINES.put(vehicleClass, SOUND_EVENTS.register(name,
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HtzCars.MOD_ID, name))));
        }
    }

    private ModSounds() {
    }

    public static DeferredHolder<SoundEvent, SoundEvent> engine(VehicleClass vehicleClass) {
        return ENGINES.get(vehicleClass);
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
