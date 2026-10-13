package com.heytozzz.htzcars.client.sound;

import com.heytozzz.htzcars.vehicle.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Starts the engine and drift sounds of every vehicle that appears in the world of the client. Client-only code. */
public final class VehicleSoundHandler {

    private VehicleSoundHandler() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof VehicleEntity vehicle) {
            Minecraft.getInstance().getSoundManager().play(new VehicleEngineSound(vehicle));
            Minecraft.getInstance().getSoundManager().play(new VehicleDriftSound(vehicle));
        }
    }
}
