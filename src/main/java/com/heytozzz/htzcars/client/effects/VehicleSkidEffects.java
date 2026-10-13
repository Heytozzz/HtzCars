package com.heytozzz.htzcars.client.effects;

import com.heytozzz.htzcars.vehicle.VehicleEntity;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Particles of a skidding vehicle, spawned at its rear wheels every client tick: tire smoke plus the same particles a
 * player makes when running, made of the block under the vehicle (so they change with the ground). On water it
 * sprays water-colored clouds instead. Client-only code, called from the vehicle through its client tick hook.
 */
public final class VehicleSkidEffects {

    /** Rear wheels, in blocks: how far behind the center of the vehicle and how far to each side. */
    private static final double REAR_AXLE_DISTANCE = 0.5;
    private static final double HALF_TRACK = 0.42;

    private VehicleSkidEffects() {
    }

    public static void tick(VehicleEntity vehicle) {
        if (!vehicle.isSkidding()) {
            return;
        }
        Level level = vehicle.level();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float intensity = vehicle.getSkidIntensity();
        double motionX = vehicle.getObservedDx();
        double motionZ = vehicle.getObservedDz();

        double yawRad = Math.toRadians(vehicle.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = -Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);

        boolean spray = vehicle.isInWater();
        BlockState ground = vehicle.getBlockStateOn();

        for (int side = -1; side <= 1; side += 2) {
            double x = vehicle.getX() - forwardX * REAR_AXLE_DISTANCE + rightX * HALF_TRACK * side + (random.nextDouble() - 0.5) * 0.2;
            double y = vehicle.getY() + 0.05;
            double z = vehicle.getZ() - forwardZ * REAR_AXLE_DISTANCE + rightZ * HALF_TRACK * side + (random.nextDouble() - 0.5) * 0.2;

            if (spray) {
                level.addParticle(ParticleTypes.CLOUD, x, y + 0.1, z, -motionX * 0.3, 0.08, -motionZ * 0.3);
                continue;
            }

            // Tire smoke.
            if (random.nextDouble() < 0.35 + 0.5 * intensity) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, -motionX * 0.15, 0.04, -motionZ * 0.15);
            }
            if (random.nextDouble() < 0.15 * intensity) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x, y + 0.05, z, -motionX * 0.1, 0.05, -motionZ * 0.1);
            }

            // Particles of the block under the vehicle, like the ones a running player kicks up.
            if (!ground.isAir()) {
                int count = intensity > 0.6F ? 2 : 1;
                for (int i = 0; i < count; i++) {
                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground), x, y + 0.05, z,
                            motionX * -4.0, 1.5, motionZ * -4.0);
                }
            }
        }
    }
}
