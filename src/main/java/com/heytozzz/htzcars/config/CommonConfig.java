package com.heytozzz.htzcars.config;

import com.heytozzz.htzcars.vehicle.VehicleClass;
import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings loaded at the start on both sides (htzcars-common.toml). Only the ones of the server (or of the single player
 * game) matter, because recipes are read there. It is a common config and not a server one on purpose: a server config
 * is loaded after the recipes, so it could not turn them off. Common code.
 */
public final class CommonConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final Map<VehicleClass, ModConfigSpec.ConfigValue<Boolean>> CRAFTING_ENABLED = new EnumMap<>(VehicleClass.class);

    static {
        BUILDER.comment("Crafting of the vehicles, one setting per class.",
                "It affects the recipes that have the htzcars:vehicle_class_enabled condition.",
                "After changing it, restart the server or run /reload.").push("crafting");
        for (VehicleClass vehicleClass : VehicleClass.values()) {
            CRAFTING_ENABLED.put(vehicleClass, BUILDER
                    .comment("true: the recipes of the " + vehicleClass.getId() + " vehicles are loaded. false: they are not.")
                    .define(vehicleClass.getId(), true, value -> value instanceof Boolean));
        }
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CommonConfig() {
    }

    /** Whether the recipes of a vehicle class are loaded. True if the config is not loaded yet. */
    public static boolean isCraftingEnabled(VehicleClass vehicleClass) {
        try {
            return CRAFTING_ENABLED.get(vehicleClass).get();
        } catch (IllegalStateException e) {
            return true;
        }
    }
}
