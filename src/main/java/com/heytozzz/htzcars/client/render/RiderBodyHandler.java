package com.heytozzz.htzcars.client.render;

import com.heytozzz.htzcars.vehicle.VehicleEntity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Keeps the body of whoever drives a vehicle facing the front of the vehicle. Only the head follows the camera, so
 * looking back does not turn the whole body around. Runs for every player on this client (the local one and the
 * others), after the player has been ticked, so it wins over the game's own body rotation. Client-only code.
 */
public final class RiderBodyHandler {

    private RiderBodyHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity rider)) {
            return;
        }
        if (!(rider.getVehicle() instanceof VehicleEntity vehicle) || vehicle.getControllingPassenger() != rider) {
            return;
        }
        // Same rotation and same previous rotation as the vehicle, so the body turns smoothly with it.
        rider.yBodyRot = vehicle.getYRot();
        rider.yBodyRotO = vehicle.yRotO;
    }
}
