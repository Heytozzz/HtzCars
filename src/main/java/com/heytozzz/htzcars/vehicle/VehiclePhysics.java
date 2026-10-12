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

    /** Drift: minimum speed to start one, and the speed below which it ends. */
    public static final double DRIFT_MIN_SPEED = 0.25;
    public static final double DRIFT_END_SPEED = 0.12;
    /** Drift: steering needed (0 to 1) when the drift key is pressed to start a drift in that direction. */
    public static final float DRIFT_START_STEER = 0.3F;
    /** Drift: vertical speed of the hop that starts it (0.18 is a hop of about 0.2 blocks). */
    public static final double DRIFT_HOP_SPEED = 0.18;
    /** Drift: how much of the angle between the vehicle and its movement is closed each tick (grip). */
    public static final float DRIFT_GRIP = 0.06F;
    /** Same, after the drift key is released: the vehicle recovers its grip. */
    public static final float DRIFT_RECOVER_GRIP = 0.25F;
    /**
     * Drift: how much of the full steering the wheels keep while drifting, depending on what the player does.
     * The angle between the vehicle and its movement settles higher the more steering is kept: with the keys
     * released it settles at about 40 degrees, steering into the drift it can go past 90 at top speed.
     */
    public static final float DRIFT_STEER_AGAINST = 0.10F;
    public static final float DRIFT_STEER_NEUTRAL = 0.35F;
    public static final float DRIFT_STEER_INTO = 0.75F;
    /** Drift: if the angle between the vehicle and its movement goes past this, the vehicle spins out and ends up in reverse. */
    public static final float DRIFT_SPIN_OUT_DEGREES = 90.0F;
    /** Drift: the vehicle turns this many times faster than it would at the same steering and speed. */
    public static final double DRIFT_YAW_BOOST = 1.3;
    /** Drift: fraction of the speed kept each tick. */
    public static final double DRIFT_DRAG = 0.992;
    /** Below this angle (degrees) between the vehicle and its movement, the slide counts as over. */
    public static final float SLIP_END_DEGREES = 0.5F;

    /** Lean of the vehicle while it slides: maximum angle, the slide that gives it, and its response (degrees per tick). */
    public static final float MAX_TILT_DEGREES = 10.0F;
    public static final float TILT_DEAD_ZONE_DEGREES = 3.0F;
    public static final float TILT_FULL_SLIP_DEGREES = 35.0F;
    public static final float TILT_RESPONSE_DEGREES = 2.0F;
    public static final double MIN_TILT_SPEED = 0.05;

    /**
     * Gravity, in blocks per tick squared. It is our own value on purpose: a plain entity has no gravity of its own
     * (mobs, items and boats each define theirs), so Entity.getGravity() cannot be used here.
     */
    public static final double GRAVITY = 0.08;
    /** Fraction of the vertical speed kept each tick. */
    public static final double VERTICAL_DRAG = 0.98;

    /** Terrain: the snow vehicle on normal ground can only reach this fraction of the top speed. */
    public static final double FROST_LAND_SPEED_FACTOR = 0.6;
    /** Speed lost per tick when the vehicle is faster than its current top speed and keeps accelerating. */
    public static final double OVERSPEED_DECELERATION = 0.01;
    /** Without traction (a land vehicle in water, the water vehicle on land): fraction of the speed kept each tick. */
    public static final double NO_TRACTION_WATER_DRAG = 0.992;
    public static final double NO_TRACTION_GROUND_DRAG = 0.93;
    /** Floating: how deep the hitbox sits in the water (blocks), how hard it is pushed back to that depth, and damping. */
    public static final double FLOAT_DEPTH = 0.35;
    public static final double BUOYANCY = 0.25;
    public static final double WATER_VERTICAL_DRAG = 0.6;

    /** Steps: the highest block each class climbs without jumping (1.0 = a full block, 0.5 = slabs and stairs). */
    public static final float STEP_HEIGHT_TRAIL = 1.0F;
    public static final float STEP_HEIGHT_CIRCUIT = 0.5F;
    public static final float STEP_HEIGHT_FROST = 0.5F;
    /** The snow vehicle climbs a full block when the block in front is snow or ice. */
    public static final float STEP_HEIGHT_FROST_ON_SNOW = 1.0F;
    public static final float STEP_HEIGHT_TIDE = 0.0F;
    /** Distances in front of the vehicle (blocks, from its center) where the snow vehicle looks for snow or ice. */
    public static final double[] STEP_PROBE_DISTANCES = {0.8, 1.2};

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
