package com.heytozzz.htzcars.client.input;

import com.heytozzz.htzcars.HtzCars;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/** The configurable vehicle keys (Options > Controls > HtzCars). Client-only code. */
public final class VehicleKeyMappings {

    private static final String CATEGORY = "key.categories." + HtzCars.MOD_ID;

    public static final KeyMapping ACCELERATE = create("accelerate", InputConstants.KEY_W);
    public static final KeyMapping BRAKE = create("brake", InputConstants.KEY_S);
    public static final KeyMapping STEER_LEFT = create("steer_left", InputConstants.KEY_A);
    public static final KeyMapping STEER_RIGHT = create("steer_right", InputConstants.KEY_D);
    /** Switches between steering with the keys and with the mouse. The default key is R (GLFW key code 82). */
    public static final KeyMapping TOGGLE_STEERING_MODE = create("toggle_steering_mode", 82);

    private VehicleKeyMappings() {
    }

    private static KeyMapping create(String name, int defaultKey) {
        return new KeyMapping("key." + HtzCars.MOD_ID + "." + name, VehicleKeyConflictContext.IN_VEHICLE,
                InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ACCELERATE);
        event.register(BRAKE);
        event.register(STEER_LEFT);
        event.register(STEER_RIGHT);
        event.register(TOGGLE_STEERING_MODE);
    }
}
