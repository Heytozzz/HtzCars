package com.heytozzz.htzcars;

import com.heytozzz.htzcars.config.ServerConfig;
import com.heytozzz.htzcars.registry.ModCreativeTabs;
import com.heytozzz.htzcars.registry.ModDataComponents;
import com.heytozzz.htzcars.registry.ModEntities;
import com.heytozzz.htzcars.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the HtzCars mod. It runs on both sides, so it must never reference client-only classes.
 * Client-only code lives in the "client" package.
 */
@Mod(HtzCars.MOD_ID)
public class HtzCars {

    /** Must match the mod id declared in neoforge.mods.toml (mod_id in gradle.properties). */
    public static final String MOD_ID = "htzcars";

    private static final Logger LOGGER = LoggerFactory.getLogger(HtzCars.class);

    public HtzCars(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("HtzCars is starting (mod id: {})", MOD_ID);

        // Per-world settings, synced from the server to the clients.
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // The data component is registered first because the creative tab builds stacks that use it.
        ModDataComponents.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
    }
}
