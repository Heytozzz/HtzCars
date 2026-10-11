package com.heytozzz.htzcars.vehicle;

/**
 * Tuning values of the arcade driving model. Speeds are in blocks per tick (1 block/tick = 20 blocks/second).
 * Everything that changes how the vehicles feel is here. Common code.
 */
public final class VehiclePhysics {

    /** Top speed forward (0.6 = 12 blocks per second). */
    public static final double MAX_SPEED = 0.6;
    /** Top speed in reverse. */
    public static final double MAX_REVERSE_SPEED = 0.25;
    /** Speed gained per tick while accelerating. */
    public static final double ACCELERATION = 0.02;
    /** Speed gained per tick while reversing. */
    public static final double REVERSE_ACCELERATION = 0.015;
    /** Speed lost per tick while braking. */
    public static final double BRAKE_FORCE = 0.04;
    /** Fraction of the speed kept each tick when no key is pressed. */
    public static final double COAST_DRAG = 0.97;
    /** Below this speed the vehicle counts as stopped. */
    public static final double STOP_THRESHOLD = 0.003;

    /** Distance between the front and the rear axle, in blocks. */
    public static final double WHEELBASE = 1.34;
    /** Maximum steering angle of the front wheels, in radians (about 31 degrees). */
    public static final double MAX_STEER_ANGLE = 0.55;
    /** How much the steering angle shrinks as the speed grows (higher = less turning at speed). */
    public static final double STEER_SPEED_FALLOFF = 1.5;
    /** How fast the steering reaches the pressed direction, per tick (1 = instantly). */
    public static final float STEER_RESPONSE = 0.2F;

    /** Mouse steering: angle between the camera and the vehicle that gives no steering at all... */
    public static final float MOUSE_STEER_DEAD_ZONE_DEGREES = 2.0F;
    /** ...and the angle that gives full steering lock. */
    public static final float MOUSE_STEER_FULL_LOCK_DEGREES = 40.0F;

    /** Fraction of the vertical speed kept each tick. */
    public static final double VERTICAL_DRAG = 0.98;

    /** Limit and response of the steering angle shown on the wheels. */
    public static final double MAX_VISUAL_STEER = 0.6;
    public static final double MIN_VISUAL_STEER_SPEED = 0.02;
    public static final float VISUAL_STEER_RESPONSE = 0.15F;

    private VehiclePhysics() {
    }

    /** Moves value towards target by at most step. */
    public static float approach(float value, float target, float step) {
        if (value < target) {
            return Math.min(value + step, target);
        }
        return Math.max(value - step, target);
    }
}
