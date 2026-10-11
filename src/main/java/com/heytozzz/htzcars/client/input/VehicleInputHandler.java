package com.heytozzz.htzcars.client.input;

import com.heytozzz.htzcars.client.ClientConfig;
import com.heytozzz.htzcars.config.ServerConfig;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import com.heytozzz.htzcars.vehicle.VehiclePhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Reads the vehicle keys every client tick and gives them to the vehicle the player is driving. Client-only code. */
public final class VehicleInputHandler {

    private VehicleInputHandler() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.isPaused()) {
            return;
        }
        if (!(player.getVehicle() instanceof VehicleEntity vehicle) || vehicle.getControllingPassenger() != player) {
            return;
        }

        while (VehicleKeyMappings.TOGGLE_STEERING_MODE.consumeClick()) {
            ClientConfig.toggleMouseSteering();
        }

        float steer;
        float steerResponse;
        boolean cameraFollows;
        if (ClientConfig.isMouseSteering()) {
            // Like an elytra: the vehicle turns towards where the camera looks, with the delay set by the server.
            float difference = Mth.wrapDegrees(player.getYRot() - vehicle.getYRot());
            float beyondDeadZone = Math.max(0.0F, Math.abs(difference) - VehiclePhysics.MOUSE_STEER_DEAD_ZONE_DEGREES);
            float fullLockRange = VehiclePhysics.MOUSE_STEER_FULL_LOCK_DEGREES - VehiclePhysics.MOUSE_STEER_DEAD_ZONE_DEGREES;
            steer = Math.signum(difference) * Mth.clamp(beyondDeadZone / fullLockRange, 0.0F, 1.0F);
            int delayTicks = ServerConfig.getMouseSteeringDelayTicks();
            steerResponse = delayTicks <= 0 ? 1.0F : 1.0F / delayTicks;
            cameraFollows = false;
        } else {
            steer = (VehicleKeyMappings.STEER_RIGHT.isDown() ? 1.0F : 0.0F)
                    - (VehicleKeyMappings.STEER_LEFT.isDown() ? 1.0F : 0.0F);
            steerResponse = VehiclePhysics.STEER_RESPONSE;
            cameraFollows = true;
        }
        vehicle.setInput(VehicleKeyMappings.ACCELERATE.isDown(), VehicleKeyMappings.BRAKE.isDown(), steer, steerResponse, cameraFollows);
    }
}
