package com.heytozzz.htzcars.vehicle;

import net.minecraft.util.Mth;

/**
 * What the driver is asking the vehicle to do this tick. The client of the driver builds it from the keys or the
 * mouse and gives it to the vehicle. Common code, so it never reads the keyboard itself.
 *
 * @param accelerate    accelerate key held
 * @param brake         brake / reverse key held
 * @param steer         -1 = full left, 1 = full right
 * @param steerResponse how much of the way to the wanted steering the wheels cover each tick (1 = instantly)
 * @param cameraFollows whether the camera of the driver turns with the vehicle
 * @param drift         drift key held
 */
public record DriverInput(boolean accelerate, boolean brake, float steer, float steerResponse,
                          boolean cameraFollows, boolean drift) {

    /** No input at all (nobody is driving). */
    public static final DriverInput NONE =
            new DriverInput(false, false, 0.0F, VehiclePhysics.STEER_RESPONSE, true, false);

    public DriverInput {
        steer = Mth.clamp(steer, -1.0F, 1.0F);
        steerResponse = Mth.clamp(steerResponse, 0.01F, 1.0F);
    }
}
