package com.heytozzz.htzcars.client.input;

import com.heytozzz.htzcars.vehicle.VehicleEntity;
import net.minecraft.client.Minecraft;
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
        float steer = (VehicleKeyMappings.STEER_RIGHT.isDown() ? 1.0F : 0.0F)
                - (VehicleKeyMappings.STEER_LEFT.isDown() ? 1.0F : 0.0F);
        vehicle.setInput(VehicleKeyMappings.ACCELERATE.isDown(), VehicleKeyMappings.BRAKE.isDown(), steer);
    }
}
