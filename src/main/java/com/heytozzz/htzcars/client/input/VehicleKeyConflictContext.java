package com.heytozzz.htzcars.client.input;

import com.heytozzz.htzcars.vehicle.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/**
 * The vehicle keys are only active while riding a vehicle, so they can share keys (W, A, S, D...) with the normal
 * movement keys without showing up as conflicts in the controls menu. Client-only code.
 */
public enum VehicleKeyConflictContext implements IKeyConflictContext {
    IN_VEHICLE;

    @Override
    public boolean isActive() {
        Player player = Minecraft.getInstance().player;
        return player != null && player.getVehicle() instanceof VehicleEntity;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return other == this || other == KeyConflictContext.UNIVERSAL;
    }
}
