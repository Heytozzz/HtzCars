package com.heytozzz.htzcars.vehicle;

import com.heytozzz.htzcars.registry.ModDataComponents;
import com.heytozzz.htzcars.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Base entity of every vehicle. It holds its class, color and model, and handles the player interactions:
 * dye it, pick it up (sneak + use) and ride it (a single seat). It also drives itself: the client of the
 * driver simulates the movement (like boats do) and the server simulates vehicles without a driver.
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

    // Driver input, written by the client of the driver every tick (never by the server).
    private boolean inputAccelerate;
    private boolean inputBrake;
    private float inputSteer;
    private float inputSteerResponse = VehiclePhysics.STEER_RESPONSE;
    private boolean inputCameraFollows = true;

    /** Smoothed steering (-1 = full left, 1 = full right). */
    private float steerState;

    // State used to animate the wheels and the steering wheel; computed on every side from the real movement.
    private boolean animInitialized;
    private double animLastX;
    private double animLastZ;
    private float animLastYaw;
    private float rolledDistance;
    private float rolledDistanceO;
    private float visualSteer;
    private float visualSteerO;

    // Smoothing of the position received from the server, for clients that are not driving.
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private float lerpYRot;

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

    /**
     * Called every tick by the client of the driver.
     * steer: -1 = left, 1 = right. steerResponse: how much of the way to the pressed steering the wheels
     * cover each tick (1 = instantly). cameraFollows: whether the camera of the driver turns with the vehicle.
     */
    public void setInput(boolean accelerate, boolean brake, float steer, float steerResponse, boolean cameraFollows) {
        this.inputAccelerate = accelerate;
        this.inputBrake = brake;
        this.inputSteer = Mth.clamp(steer, -1.0F, 1.0F);
        this.inputSteerResponse = Mth.clamp(steerResponse, 0.01F, 1.0F);
        this.inputCameraFollows = cameraFollows;
    }

    /** Distance the vehicle has rolled, in blocks, interpolated for rendering. */
    public float getRolledDistance(float partialTick) {
        return Mth.lerp(partialTick, rolledDistanceO, rolledDistance);
    }

    /** Steering angle of the front wheels in radians (positive = right), interpolated for rendering. */
    public float getVisualSteer(float partialTick) {
        return Mth.lerp(partialTick, visualSteerO, visualSteer);
    }

    /** The first passenger drives, as long as it is a living entity. */
    @Override
    public LivingEntity getControllingPassenger() {
        if (!getPassengers().isEmpty() && getPassengers().get(0) instanceof LivingEntity driver) {
            return driver;
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (getControllingPassenger() == null) {
            inputAccelerate = false;
            inputBrake = false;
            inputSteer = 0.0F;
            inputSteerResponse = VehiclePhysics.STEER_RESPONSE;
            inputCameraFollows = true;
        }

        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            drive();
        } else if (level().isClientSide) {
            interpolateToServerPosition();
        }
        updateAnimationState();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpSteps = Math.max(1, steps);
    }

    private void interpolateToServerPosition() {
        if (lerpSteps <= 0) {
            return;
        }
        double newX = getX() + (lerpX - getX()) / lerpSteps;
        double newY = getY() + (lerpY - getY()) / lerpSteps;
        double newZ = getZ() + (lerpZ - getZ()) / lerpSteps;
        float newYaw = getYRot() + Mth.wrapDegrees(lerpYRot - getYRot()) / lerpSteps;
        lerpSteps--;
        setPos(newX, newY, newZ);
        setYRot(newYaw);
        setXRot(0.0F);
    }

    /** One tick of arcade driving. Runs only where the vehicle is simulated (see isControlledByLocalInstance). */
    private void drive() {
        Vec3 motion = getDeltaMovement();
        double yawRad = Math.toRadians(getYRot());
        // Speed along the direction the vehicle is facing; negative when going backwards.
        double speed = motion.x * -Math.sin(yawRad) + motion.z * Math.cos(yawRad);

        if (inputBrake) {
            if (speed > VehiclePhysics.STOP_THRESHOLD) {
                speed = Math.max(0.0, speed - VehiclePhysics.BRAKE_FORCE);
            } else {
                speed = Math.max(-VehiclePhysics.MAX_REVERSE_SPEED, speed - VehiclePhysics.REVERSE_ACCELERATION);
            }
        } else if (inputAccelerate) {
            if (speed < 0.0) {
                speed = Math.min(0.0, speed + VehiclePhysics.BRAKE_FORCE);
            } else {
                speed = Math.min(VehiclePhysics.MAX_SPEED, speed + VehiclePhysics.ACCELERATION);
            }
        } else {
            speed *= VehiclePhysics.COAST_DRAG;
            if (Math.abs(speed) < VehiclePhysics.STOP_THRESHOLD) {
                speed = 0.0;
            }
        }

        // Bicycle model: the turning rate comes from the speed and the angle of the front wheels.
        steerState = VehiclePhysics.approach(steerState, inputSteer, inputSteerResponse);
        double steerAngle = steerState * VehiclePhysics.MAX_STEER_ANGLE
                / (1.0 + Math.abs(speed) * VehiclePhysics.STEER_SPEED_FALLOFF);
        double yawRate = speed / VehiclePhysics.WHEELBASE * Math.tan(steerAngle);
        float oldYaw = getYRot();
        float newYaw = Mth.wrapDegrees(oldYaw + (float) Math.toDegrees(yawRate));
        setYRot(newYaw);
        setXRot(0.0F);

        // With key steering the camera of the driver turns with the vehicle; with mouse steering it stays free.
        LivingEntity driver = getControllingPassenger();
        float yawChange = Mth.wrapDegrees(newYaw - oldYaw);
        if (driver != null && inputCameraFollows && yawChange != 0.0F) {
            driver.setYRot(driver.getYRot() + yawChange);
            driver.yRotO += yawChange;
        }

        // Arcade grip: the speed always points where the vehicle faces.
        yawRad = Math.toRadians(newYaw);
        double verticalSpeed = (motion.y - getGravity()) * VehiclePhysics.VERTICAL_DRAG;
        setDeltaMovement(-Math.sin(yawRad) * speed, verticalSpeed, Math.cos(yawRad) * speed);
        move(MoverType.SELF, getDeltaMovement());
    }

    /** Updates how much the wheels have rolled and how much the front wheels are turned, from the real movement. */
    private void updateAnimationState() {
        double x = getX();
        double z = getZ();
        float yaw = getYRot();

        rolledDistanceO = rolledDistance;
        visualSteerO = visualSteer;
        if (animInitialized) {
            double yawRad = Math.toRadians(yaw);
            double forwardMove = (x - animLastX) * -Math.sin(yawRad) + (z - animLastZ) * Math.cos(yawRad);
            rolledDistance += (float) forwardMove;

            double target = 0.0;
            if (Math.abs(forwardMove) > VehiclePhysics.MIN_VISUAL_STEER_SPEED) {
                double yawChange = Math.toRadians(Mth.wrapDegrees(yaw - animLastYaw));
                target = Mth.clamp(Math.atan(VehiclePhysics.WHEELBASE * yawChange / forwardMove),
                        -VehiclePhysics.MAX_VISUAL_STEER, VehiclePhysics.MAX_VISUAL_STEER);
            }
            visualSteer = VehiclePhysics.approach(visualSteer, (float) target, VehiclePhysics.VISUAL_STEER_RESPONSE);
        }
        animLastX = x;
        animLastZ = z;
        animLastYaw = yaw;
        animInitialized = true;
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
