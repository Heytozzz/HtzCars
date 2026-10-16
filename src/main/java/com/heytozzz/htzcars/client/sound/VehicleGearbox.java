package com.heytozzz.htzcars.client.sound;

import com.heytozzz.htzcars.vehicle.VehiclePhysics;
import net.minecraft.util.Mth;

/**
 * The automatic gearbox of a vehicle. It is only for ambience: the gear comes from the speed of the vehicle and only
 * changes how the engine sounds (the revs rise inside each gear and drop when it shifts up), it never changes how the
 * vehicle drives. There is one gearbox per vehicle, owned by its engine sound. Client-only code.
 */
public final class VehicleGearbox {

    /**
     * Speed at which each upshift happens, as a fraction of the top speed (0 to 1): element 0 is the shift from
     * gear 1 to gear 2, element 1 from gear 2 to gear 3, and so on. The number of gears is this length plus one,
     * so add or remove values to change it. The values must increase.
     */
    private static final float[] UPSHIFT_SPEED = {0.22F, 0.45F, 0.70F};
    /** A downshift happens this far below the speed of the matching upshift, so the gear does not flap back and forth. */
    private static final float DOWNSHIFT_HYSTERESIS = 0.06F;
    /** Minimum ticks between two shifts. */
    private static final int MIN_TICKS_BETWEEN_SHIFTS = 6;
    /** Engine revs (0 to 1) right after an upshift; the revs then climb back to 1 until the next one. */
    private static final float REVS_AFTER_UPSHIFT = 0.55F;
    /** After a shift the engine is cut for this many ticks, and its volume is multiplied by the factor. */
    private static final int SHIFT_CUT_TICKS = 3;
    private static final float SHIFT_CUT_VOLUME = 0.7F;

    /** The vehicle counts as going in reverse when it moves backwards faster than this (blocks per tick). */
    public static final double REVERSE_DETECT_SPEED = 0.03;

    public static final int GEAR_COUNT = UPSHIFT_SPEED.length + 1;

    private int gear = 1;
    private boolean reversing;
    private int ticksSinceShift = MIN_TICKS_BETWEEN_SHIFTS;
    private int cutTicksLeft;
    private float revs;

    /**
     * Call once per client tick while somebody drives. speed: horizontal speed in blocks per tick (always positive);
     * reversing: whether the vehicle moves backwards. Returns +1 if it shifted up this tick, -1 if it shifted down
     * and 0 if the gear did not change. In reverse there are no shifts: the revs just climb with the speed.
     */
    public int update(double speed, boolean reversing) {
        this.reversing = reversing;
        ticksSinceShift++;
        if (cutTicksLeft > 0) {
            cutTicksLeft--;
        }

        int shift = 0;
        float fraction;
        if (reversing) {
            gear = 1;
            // The reverse speed range is mapped onto the range of the first gear.
            fraction = Mth.clamp((float) (speed / VehiclePhysics.MAX_REVERSE_SPEED), 0.0F, 1.0F) * UPSHIFT_SPEED[0];
        } else {
            fraction = Mth.clamp((float) (speed / VehiclePhysics.MAX_SPEED), 0.0F, 1.0F);
            if (ticksSinceShift >= MIN_TICKS_BETWEEN_SHIFTS) {
                if (gear < GEAR_COUNT && fraction >= UPSHIFT_SPEED[gear - 1]) {
                    gear++;
                    shift = 1;
                } else if (gear > 1 && fraction < UPSHIFT_SPEED[gear - 2] - DOWNSHIFT_HYSTERESIS) {
                    gear--;
                    shift = -1;
                }
            }
            if (shift != 0) {
                ticksSinceShift = 0;
                cutTicksLeft = SHIFT_CUT_TICKS;
            }
        }

        // Where the speed sits inside the range of the current gear (0 to 1).
        float lower = gear == 1 ? 0.0F : UPSHIFT_SPEED[gear - 2];
        float upper = gear == GEAR_COUNT ? 1.0F : UPSHIFT_SPEED[gear - 1];
        float positionInGear = Mth.clamp((fraction - lower) / (upper - lower), 0.0F, 1.0F);
        revs = gear == 1 ? positionInGear : Mth.lerp(positionInGear, REVS_AFTER_UPSHIFT, 1.0F);
        return shift;
    }

    /** Back to first gear and idle, for when nobody drives. */
    public void reset() {
        gear = 1;
        reversing = false;
        ticksSinceShift = MIN_TICKS_BETWEEN_SHIFTS;
        cutTicksLeft = 0;
        revs = 0.0F;
    }

    /** Current forward gear (1 to GEAR_COUNT); it is 1 while reversing. */
    public int getGear() {
        return gear;
    }

    public boolean isReversing() {
        return reversing;
    }

    /** Engine revs from 0 (idle) to 1 (redline of the current gear). */
    public float getRevs() {
        return revs;
    }

    /** Multiplier for the engine volume: lower for a few ticks after each shift, as if the throttle was lifted. */
    public float getVolumeFactor() {
        return cutTicksLeft > 0 ? SHIFT_CUT_VOLUME : 1.0F;
    }
}
