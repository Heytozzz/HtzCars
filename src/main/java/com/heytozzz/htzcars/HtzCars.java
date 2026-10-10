package com.heytozzz.htzcars;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the HtzCars mod. For now it only logs a startup message.
 */
@Mod(HtzCars.MOD_ID)
public class HtzCars {

    /** Must match the mod id declared in neoforge.mods.toml (mod_id in gradle.properties). */
    public static final String MOD_ID = "htzcars";

    private static final Logger LOGGER = LoggerFactory.getLogger(HtzCars.class);

    public HtzCars(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("HtzCars is starting (mod id: {})", MOD_ID);
    }
}
