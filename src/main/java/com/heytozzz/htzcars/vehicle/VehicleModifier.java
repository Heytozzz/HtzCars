package com.heytozzz.htzcars.vehicle;

/**
 * A change to how a vehicle drives, to be applied for some time with VehicleEntity.addModifier (boosts, slowdowns,
 * slippery or sticky ground...). Every factor multiplies the normal value, so 1.0 means no change. Several
 * modifiers on the same vehicle multiply together. Common code.
 *
 * @param maxSpeedFactor     top speed (also the top speed in reverse)
 * @param accelerationFactor how fast it speeds up
 * @param brakeFactor        how hard it brakes
 * @param gripFactor         how fast the movement follows where the vehicle faces while it slides (lower = slides more)
 */
public record VehicleModifier(double maxSpeedFactor, double accelerationFactor, double brakeFactor, double gripFactor) {

    /** No change at all. */
    public static final VehicleModifier NONE = new VehicleModifier(1.0, 1.0, 1.0, 1.0);

    /** A change of the top speed and of the acceleration only (for example a boost: 1.5 and 3.0). */
    public static VehicleModifier speed(double maxSpeedFactor, double accelerationFactor) {
        return new VehicleModifier(maxSpeedFactor, accelerationFactor, 1.0, 1.0);
    }
}
