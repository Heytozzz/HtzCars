package com.heytozzz.htzcars.vehicle;

import com.heytozzz.htzcars.registry.ModDataComponents;
import com.heytozzz.htzcars.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base entity of every vehicle. It holds its class, color and model, and handles the player interactions:
 * dye it, pick it up (sneak + use) and ride it (a single seat). It does not move yet.
 * Common code: it must not reference client-only classes.
 */
public class VehicleEntity extends Entity {

    private static final String TAG_COLOR = "Color";
    private static final String TAG_MODEL = "Model";

    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODEL =
            SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);

    private final VehicleClass vehicleClass;

    public VehicleEntity(EntityType<?> type, Level level, VehicleClass vehicleClass) {
        super(type, level);
        this.vehicleClass = vehicleClass;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLOR));
    }

    public void setColor(DyeColor color) {
        this.entityData.set(DATA_COLOR, color.getId());
    }

    /** Index of the model inside the vehicle class (each class will have 1 to 3 models). */
    public int getModelIndex() {
        return this.entityData.get(DATA_MODEL);
    }

    public void setModelIndex(int modelIndex) {
        this.entityData.set(DATA_MODEL, modelIndex);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    /** The item that gives this vehicle back, with its color (also used by the creative pick block). */
    @Override
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(ModItems.get(vehicleClass));
        stack.set(ModDataComponents.VEHICLE_COLOR.get(), getColor());
        return stack;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        boolean clientSide = level().isClientSide;
        ItemStack held = player.getItemInHand(hand);

        // Dye in hand: change the color.
        if (held.getItem() instanceof DyeItem dye) {
            DyeColor newColor = dye.getDyeColor();
            if (newColor == getColor()) {
                return InteractionResult.PASS;
            }
            if (!clientSide) {
                setColor(newColor);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(clientSide);
        }

        // Sneaking: pick the vehicle up, keeping its color (only when nobody is riding it).
        if (player.isShiftKeyDown() && getPassengers().isEmpty()) {
            if (!clientSide) {
                ItemStack stack = getPickResult();
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
                discard();
            }
            return InteractionResult.sidedSuccess(clientSide);
        }

        // Otherwise: ride it.
        if (clientSide) {
            return InteractionResult.SUCCESS;
        }
        return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COLOR, DyeColor.WHITE.getId());
        builder.define(DATA_MODEL, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_COLOR)) {
            setColor(DyeColor.byId(tag.getInt(TAG_COLOR)));
        }
        if (tag.contains(TAG_MODEL)) {
            setModelIndex(tag.getInt(TAG_MODEL));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(TAG_COLOR, getColor().getId());
        tag.putInt(TAG_MODEL, getModelIndex());
    }
}
