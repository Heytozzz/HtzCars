package com.heytozzz.htzcars.client.sound;

import com.heytozzz.htzcars.registry.ModSounds;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import com.heytozzz.htzcars.vehicle.VehiclePhysics;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * The engine of a vehicle: a sound that loops while the vehicle exists, follows it, and gets higher and louder
 * with its revs. The revs come from the automatic gearbox (see VehicleGearbox), so the pitch climbs inside each gear
 * and drops when it shifts up. It is silent while nobody is driving. Client-only code.
 */
public class VehicleEngineSound extends AbstractTickableSoundInstance {

    private static final float IDLE_PITCH = 0.6F;
    private static final float MAX_PITCH = 1.5F;
    private static final float IDLE_VOLUME = 0.5F;
    private static final float MAX_VOLUME = 0.9F;
    /** How fast the pitch follows the revs, per tick (so a shift is heard as a quick drop instead of a jump). */
    private static final float PITCH_RISE_PER_TICK = 0.15F;
    private static final float PITCH_FALL_PER_TICK = 0.15F;
    /** How fast the volume follows its target, per tick. */
    private static final float VOLUME_STEP_PER_TICK = 0.1F;
    /**
     * Volume while nobody drives. It is not 0 on purpose: the game does not start a sound whose volume is 0 when it
     * is played (and this one starts when the vehicle appears, before anybody rides it), so it would never play.
     */
    private static final float SILENT_VOLUME = 0.001F;

    private final VehicleEntity vehicle;
    private final VehicleGearbox gearbox = new VehicleGearbox();

    public VehicleEngineSound(VehicleEntity vehicle) {
        super(ModSounds.engine(vehicle.getVehicleClass()).get(), SoundSource.PLAYERS, RandomSource.create());
        this.vehicle = vehicle;
        this.looping = true;
        this.delay = 0;
        this.volume = SILENT_VOLUME;
        this.pitch = IDLE_PITCH;
        this.x = vehicle.getX();
        this.y = vehicle.getY();
        this.z = vehicle.getZ();
    }

    @Override
    public void tick() {
        if (vehicle.isRemoved()) {
            stop();
            return;
        }
        this.x = vehicle.getX();
        this.y = vehicle.getY();
        this.z = vehicle.getZ();

        if (vehicle.getControllingPassenger() == null) {
            gearbox.reset();
            this.volume = SILENT_VOLUME;
            this.pitch = IDLE_PITCH;
            return;
        }

        double speed = vehicle.getObservedSpeed();
        // Forward direction of the vehicle; a movement against it means reverse.
        double yawRad = Math.toRadians(vehicle.getYRot());
        double forwardSpeed = vehicle.getObservedDx() * -Math.sin(yawRad) + vehicle.getObservedDz() * Math.cos(yawRad);
        gearbox.update(speed, forwardSpeed < -VehicleGearbox.REVERSE_DETECT_SPEED);

        float targetPitch = Mth.lerp(gearbox.getRevs(), IDLE_PITCH, MAX_PITCH);
        float pitchStep = targetPitch > this.pitch ? PITCH_RISE_PER_TICK : PITCH_FALL_PER_TICK;
        this.pitch = VehiclePhysics.approach(this.pitch, targetPitch, pitchStep);

        float speedFraction = Mth.clamp((float) (speed / VehiclePhysics.MAX_SPEED), 0.0F, 1.0F);
        float targetVolume = Mth.lerp(speedFraction, IDLE_VOLUME, MAX_VOLUME) * gearbox.getVolumeFactor();
        this.volume = VehiclePhysics.approach(this.volume, targetVolume, VOLUME_STEP_PER_TICK);
    }
}
