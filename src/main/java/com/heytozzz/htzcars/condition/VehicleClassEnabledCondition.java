package com.heytozzz.htzcars.condition;

import com.heytozzz.htzcars.config.CommonConfig;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recipe condition: true when the crafting of a vehicle class is enabled in the config. In a recipe:
 * "neoforge:conditions": [ { "type": "htzcars:vehicle_class_enabled", "vehicle_class": "trail" } ]
 * Common code.
 */
public record VehicleClassEnabledCondition(String vehicleClass) implements ICondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleClassEnabledCondition.class);

    public static final MapCodec<VehicleClassEnabledCondition> CODEC = Codec.STRING.fieldOf("vehicle_class")
            .xmap(VehicleClassEnabledCondition::new, VehicleClassEnabledCondition::vehicleClass);

    @Override
    public boolean test(ICondition.IContext context) {
        for (VehicleClass candidate : VehicleClass.values()) {
            if (candidate.getId().equals(vehicleClass)) {
                return CommonConfig.isCraftingEnabled(candidate);
            }
        }
        LOGGER.warn("Unknown vehicle class '{}' in a recipe condition; the recipe is loaded anyway", vehicleClass);
        return true;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
