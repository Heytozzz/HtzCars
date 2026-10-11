package com.heytozzz.htzcars.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Per-player settings (htzcars-client.toml). Client-only code: it is only registered from HtzCarsClient. */
public final class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<Boolean> MOUSE_STEERING = BUILDER
            .comment("false: steer with the left and right keys. true: steer with the mouse (the vehicle turns",
                    "towards where the camera looks). Can also be switched in game with the toggle key.")
            .define("mouseSteering", false, value -> value instanceof Boolean);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }

    public static boolean isMouseSteering() {
        try {
            return MOUSE_STEERING.get();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /** Switches between steering with the keys and with the mouse, and saves the choice. */
    public static void toggleMouseSteering() {
        try {
            MOUSE_STEERING.set(!MOUSE_STEERING.get());
            MOUSE_STEERING.save();
        } catch (IllegalStateException e) {
            // The config is not loaded yet; nothing to change.
        }
    }
}
