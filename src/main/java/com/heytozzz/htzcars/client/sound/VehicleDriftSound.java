package com.heytozzz.htzcars.client.sound;

import com.heytozzz.htzcars.registry.ModSounds;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import com.heytozzz.htzcars.vehicle.VehiclePhysics;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * The tires screeching: a sound that loops while the vehicle exists, follows it, and is only heard while the vehicle
 * skids on the ground (louder the more it slides). Client-only code.
 */
public class VehicleDriftSound extends AbstractTickableSoundInstance {

    private static final float MIN_PITCH = 0.85F;
    private static final float MAX_PITCH = 1.2F;
    private static final float MIN_VOLUME = 0.35F;
    private static final float MAX_VOLUME = 0.9F;
    /** Not 0 on purpose: the game does not start a sound whose volume is 0 (see VehicleEngineSound). */
    private static final float SILENT_VOLUME = 0.001F;

    private final VehicleEntity vehicle;

    public VehicleDriftSound(VehicleEntity vehicle) {
        super(ModSounds.drift(vehicle.getVehicleClass()).get(), SoundSource.PLAYERS, RandomSource.create());
        this.vehicle = vehicle;
        this.looping = true;
        this.delay = 0;
        this.volume = SILENT_VOLUME;
        this.pitch = MIN_PITCH;
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

        // In the water the tires do not screech (the vehicle sprays water instead).
        if (!vehicle.isSkidding() || vehicle.isInWater()) {
            this.volume = SILENT_VOLUME;
            return;
        }
        float speedFraction = Mth.clamp((float) (vehicle.getObservedSpeed() / VehiclePhysics.MAX_SPEED), 0.0F, 1.0F);
        this.pitch = Mth.lerp(speedFraction, MIN_PITCH, MAX_PITCH);
        this.volume = Mth.lerp(vehicle.getSkidIntensity(), MIN_VOLUME, MAX_VOLUME);
    }
}
