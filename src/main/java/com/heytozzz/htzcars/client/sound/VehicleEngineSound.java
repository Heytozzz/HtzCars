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
 * with its speed. It is silent while nobody is driving. Client-only code.
 */
public class VehicleEngineSound extends AbstractTickableSoundInstance {

    private static final float IDLE_PITCH = 0.6F;
    private static final float MAX_PITCH = 1.5F;
    private static final float IDLE_VOLUME = 0.5F;
    private static final float MAX_VOLUME = 0.9F;
    /**
     * Volume while nobody drives. It is not 0 on purpose: the game does not start a sound whose volume is 0 when it
     * is played (and this one starts when the vehicle appears, before anybody rides it), so it would never play.
     */
    private static final float SILENT_VOLUME = 0.001F;

    private final VehicleEntity vehicle;

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
            this.volume = SILENT_VOLUME;
            return;
        }
        float speedFraction = Mth.clamp((float) (vehicle.getObservedSpeed() / VehiclePhysics.MAX_SPEED), 0.0F, 1.0F);
        this.pitch = Mth.lerp(speedFraction, IDLE_PITCH, MAX_PITCH);
        this.volume = Mth.lerp(speedFraction, IDLE_VOLUME, MAX_VOLUME);
    }
}
