package com.heytozzz.htzcars.registry;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.item.VehicleItem;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items: one per vehicle class (the color is stored in a data component). Right click places the vehicle. Common code. */
public final class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HtzCars.MOD_ID);

    private static final Map<VehicleClass, DeferredHolder<Item, Item>> VEHICLE_ITEMS = new EnumMap<>(VehicleClass.class);

    static {
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            VEHICLE_ITEMS.put(vehicleClass,
                    ITEMS.<Item>register(vehicleClass.getId(), () -> new VehicleItem(new Item.Properties().stacksTo(1), vehicleClass)));
        }
    }

    private ModItems() {
    }

    public static DeferredHolder<Item, Item> get(VehicleClass vehicleClass) {
        return VEHICLE_ITEMS.get(vehicleClass);
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
