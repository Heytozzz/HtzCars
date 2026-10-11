package com.heytozzz.htzcars.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side settings (htzcars-server.toml, one per world). NeoForge sends them to every client that joins,
 * so the clients use the values of the server they are playing on. Common code.
 */
public final class ServerConfig {

    private static final int DEFAULT_MOUSE_STEERING_DELAY_TICKS = 8;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<Integer> MOUSE_STEERING_DELAY_TICKS = BUILDER
            .comment("Delay, in ticks (20 ticks = 1 second), of the steering when a player steers with the mouse.",
                    "It is the time the wheels take to go from straight to full lock. 0 = instant (abrupt turns),",
                    "higher values give smoother and slower turns. Allowed values: 0 to 40.")
            .define("mouseSteeringDelayTicks", DEFAULT_MOUSE_STEERING_DELAY_TICKS,
                    value -> value instanceof Integer ticks && ticks >= 0 && ticks <= 40);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ServerConfig() {
    }

    /** Delay of the mouse steering in ticks. Falls back to the default if the config is not loaded yet. */
    public static int getMouseSteeringDelayTicks() {
        try {
            return MOUSE_STEERING_DELAY_TICKS.get();
        } catch (IllegalStateException e) {
            return DEFAULT_MOUSE_STEERING_DELAY_TICKS;
        }
    }
}
