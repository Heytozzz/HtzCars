package com.heytozzz.htzcars.item;

import com.heytozzz.htzcars.registry.ModDataComponents;
import com.heytozzz.htzcars.registry.ModEntities;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import com.heytozzz.htzcars.vehicle.VehicleEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Item that places a vehicle of one class where the player is looking (on a block or on the water surface).
 * The color stored in the stack is passed on to the vehicle. Common code.
 */
public class VehicleItem extends Item {

    private final VehicleClass vehicleClass;

    public VehicleItem(Item.Properties properties, VehicleClass vehicleClass) {
        super(properties);
        this.vehicleClass = vehicleClass;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        VehicleEntity vehicle = ModEntities.get(vehicleClass).get().create(level);
        if (vehicle == null) {
            return InteractionResultHolder.fail(stack);
        }
        Vec3 position = hit.getLocation();
        vehicle.setPos(position.x, position.y, position.z);
        vehicle.setYRot(player.getYRot());
        vehicle.setColor(stack.getOrDefault(ModDataComponents.VEHICLE_COLOR.get(), DyeColor.WHITE));

        if (!level.noCollision(vehicle, vehicle.getBoundingBox())) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            level.addFreshEntity(vehicle);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
