package com.heytozzz.htzcars.client.render;

import net.minecraft.world.item.DyeColor;

/** Tint used to color the white base texture of the vehicles. Client-only code. */
public final class VehicleTint {

    private VehicleTint() {
    }

    /** Opaque ARGB color that, multiplied with the white base texture, gives the dye color. */
    public static int argb(DyeColor color) {
        return 0xFF000000 | (color.getTextureDiffuseColor() & 0xFFFFFF);
    }
}
