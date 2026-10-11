package com.heytozzz.htzcars.registry;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Entity types: one per vehicle class. Common code. */
public final class ModEntities {

    /** Placeholder hitbox size, to be adjusted when the final models are in. */
    private static final float HITBOX_WIDTH = 1.3F;
    private static final float HITBOX_HEIGHT = 0.8F;

    /** Height of the seat cushion (about 5.4 model pixels); the passenger sits on this point. To be tuned in game. */
    private static final float SEAT_HEIGHT = 0.34F;

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HtzCars.MOD_ID);

    private static final Map<VehicleClass, DeferredHolder<EntityType<?>, EntityType<VehicleEntity>>> VEHICLES =
            new EnumMap<>(VehicleClass.class);

    static {
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            VEHICLES.put(vehicleClass, registerVehicle(vehicleClass));
        }
    }

    private ModEntities() {
    }

    private static DeferredHolder<EntityType<?>, EntityType<VehicleEntity>> registerVehicle(VehicleClass vehicleClass) {
        String id = vehicleClass.getId();
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.<VehicleEntity>of(
                        (type, level) -> new VehicleEntity(type, level, vehicleClass), MobCategory.MISC)
                .sized(HITBOX_WIDTH, HITBOX_HEIGHT)
                .passengerAttachments(SEAT_HEIGHT)
                .build(id));
    }

    public static DeferredHolder<EntityType<?>, EntityType<VehicleEntity>> get(VehicleClass vehicleClass) {
        return VEHICLES.get(vehicleClass);
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
