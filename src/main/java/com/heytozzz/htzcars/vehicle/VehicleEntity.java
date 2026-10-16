package com.heytozzz.htzcars.vehicle;

import com.heytozzz.htzcars.registry.ModDataComponents;
import com.heytozzz.htzcars.registry.ModEntities;
import com.heytozzz.htzcars.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Base entity of every vehicle. It holds its class, color and model, and handles the player interactions:
 * dye it, pick it up (sneak + use) and ride it (a single seat). It also drives itself, drift included: the
 * client of the driver simulates the movement (like boats do) and the server simulates vehicles without a driver.
 * Common code: it must not reference client-only classes.
 */
public class VehicleEntity extends Entity {

    /**
     * Called on every client tick for every vehicle. The client sets it (for the particles); on a server it stays empty,
     * so the vehicle never needs to know about client-only classes.
     */
    private static Consumer<VehicleEntity> clientTickHook = vehicle -> { };

    private static final String TAG_COLOR = "Color";
    private static final String TAG_MODEL = "Model";

    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MODEL =
            SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);

    private final VehicleClass vehicleClass;

    /** A modifier that is active on this vehicle and the ticks it has left. */
    private record ActiveModifier(VehicleModifier modifier, int remainingTicks) {
    }

    /** Modifiers (boosts, slowdowns...) that are active on this vehicle. */
    private final List<ActiveModifier> activeModifiers = new ArrayList<>();

    // Driver input, written by the client of the driver every tick (never by the server).
    private DriverInput input = DriverInput.NONE;

    /** Smoothed steering (-1 = full left, 1 = full right). */
    private float steerState;

    // Drifting, simulated only where the vehicle is driven.
    private boolean drifting;
    private int driftDirection;
    /** True while the vehicle moves in a different direction than it faces (drifting or recovering from it). */
    private boolean sliding;
    /** Direction of the movement in degrees; only differs from the facing while sliding. */
    private float moveYaw;

    // State used to animate the wheels and the steering wheel; computed on every side from the real movement.
    private boolean animInitialized;
    private double animLastX;
    private double animLastZ;
    private float animLastYaw;
    private float rolledDistance;
    private float rolledDistanceO;
    private float visualSteer;
    private float visualSteerO;
    private float visualTilt;
    private float visualTiltO;
    /** Horizontal speed in blocks per tick, measured from the real movement (so it works on every client). */
    private float observedSpeed;
    /** Angle in degrees between the way the vehicle faces and the way it moves (positive = it moves to the left of its front). */
    private float observedSlip;
    /** Movement of the last tick, in blocks. */
    private double observedDx;
    private double observedDz;

    /** Server side: whether somebody was driving on the previous tick. */
    private boolean hadDriver;

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
     * Applies a modifier to this vehicle for a number of ticks (20 ticks = 1 second). Modifiers stack by multiplying.
     * Call it where the vehicle is simulated: on the client of the driver, or on the server when nobody drives it.
     */
    public void addModifier(VehicleModifier modifier, int durationTicks) {
        if (durationTicks > 0) {
            activeModifiers.add(new ActiveModifier(modifier, durationTicks));
        }
    }

    /** Removes every modifier from this vehicle. */
    public void clearModifiers() {
        activeModifiers.clear();
    }

    /** Called every tick by the client of the driver. */
    public void setInput(DriverInput input) {
        this.input = input;
    }

    /** Distance the vehicle has rolled, in blocks, interpolated for rendering. */
    public float getRolledDistance(float partialTick) {
        return Mth.lerp(partialTick, rolledDistanceO, rolledDistance);
    }

    /** Steering angle of the front wheels in radians (positive = right), interpolated for rendering. */
    public float getVisualSteer(float partialTick) {
        return Mth.lerp(partialTick, visualSteerO, visualSteer);
    }

    /** Sets what every vehicle does on each client tick. Only the client calls this. */
    public static void setClientTickHook(Consumer<VehicleEntity> hook) {
        clientTickHook = hook;
    }

    /** Movement of the last tick, in blocks per tick. Used for the particles. */
    public double getObservedDx() {
        return observedDx;
    }

    public double getObservedDz() {
        return observedDz;
    }

    /**
     * True while the vehicle slides sideways at speed on the ground (or on the water): a drift or a skid. It is measured
     * from the real movement, so it is the same on every client.
     */
    public boolean isSkidding() {
        return (onGround() || isInWater()) && observedSpeed > VehiclePhysics.SKID_MIN_SPEED
                && Math.abs(observedSlip) > VehiclePhysics.SKID_MIN_SLIP_DEGREES;
    }

    /** How much the vehicle slides, from 0 (just started to skid) to 1 (full slide). */
    public float getSkidIntensity() {
        float range = VehiclePhysics.SKID_FULL_SLIP_DEGREES - VehiclePhysics.SKID_MIN_SLIP_DEGREES;
        return Mth.clamp((Math.abs(observedSlip) - VehiclePhysics.SKID_MIN_SLIP_DEGREES) / range, 0.0F, 1.0F);
    }

    /** Horizontal speed in blocks per tick, measured from the real movement. Used for the engine sound. */
    public float getObservedSpeed() {
        return observedSpeed;
    }

    /** Lean of the vehicle in degrees (positive = to the right), interpolated for rendering. */
    public float getVisualTilt(float partialTick) {
        return Mth.lerp(partialTick, visualTiltO, visualTilt);
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

        LivingEntity driver = getControllingPassenger();
        if (driver == null) {
            input = DriverInput.NONE;
        }

        // While somebody drives, the client of the driver simulates the vehicle and the server and the other clients
        // only follow it, so the data they hold about it gets out of date. When the driver leaves, the server replaces
        // the vehicle by a fresh copy at the position it knows, which every client receives from scratch.
        if (!level().isClientSide) {
            if (hadDriver && driver == null && replaceWithFreshCopy()) {
                return;
            }
            hadDriver = driver != null;
        }

        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            drive();
        } else if (level().isClientSide) {
            // It is not simulated here: forget the speed it had, so it does not shoot off when somebody drives it again.
            setDeltaMovement(Vec3.ZERO);
            interpolateToServerPosition();
        }
        updateAnimationState();
        if (level().isClientSide) {
            clientTickHook.accept(this);
        }
    }

    /**
     * Server side: removes this vehicle and spawns an identical one in its place that keeps rolling with the speed it
     * had, so it coasts to a stop after the driver leaves. Returns false (and changes nothing) if it could not.
     */
    private boolean replaceWithFreshCopy() {
        VehicleEntity copy = ModEntities.get(vehicleClass).get().create(level());
        if (copy == null) {
            return false;
        }
        copy.setPos(getX(), getY(), getZ());
        copy.setYRot(getYRot());
        copy.setXRot(0.0F);
        copy.setColor(getColor());
        copy.setModelIndex(getModelIndex());
        copy.setDeltaMovement(observedDx, 0.0, observedDz);
        level().addFreshEntity(copy);
        discard();
        return true;
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

    /**
     * How high a block this vehicle can climb without jumping. The game asks for it every time the vehicle moves.
     */
    @Override
    public float maxUpStep() {
        return switch (vehicleClass) {
            case TRAIL -> VehiclePhysics.STEP_HEIGHT_TRAIL;
            case CIRCUIT -> VehiclePhysics.STEP_HEIGHT_CIRCUIT;
            case FROST -> isSnowOrIceAhead() ? VehiclePhysics.STEP_HEIGHT_FROST_ON_SNOW : VehiclePhysics.STEP_HEIGHT_FROST;
            case TIDE -> VehiclePhysics.STEP_HEIGHT_TIDE;
        };
    }

    /** True if the block in the direction the vehicle is moving, at its feet, is snow or ice. */
    private boolean isSnowOrIceAhead() {
        Vec3 motion = getDeltaMovement();
        double horizontalSpeed = motion.horizontalDistance();
        if (horizontalSpeed < 1.0E-4) {
            return false;
        }
        double directionX = motion.x / horizontalSpeed;
        double directionZ = motion.z / horizontalSpeed;
        for (double distance : VehiclePhysics.STEP_PROBE_DISTANCES) {
            BlockPos pos = BlockPos.containing(getX() + directionX * distance, getY() + 0.1, getZ() + directionZ * distance);
            BlockState state = level().getBlockState(pos);
            if (state.is(BlockTags.SNOW) || state.is(BlockTags.ICE)) {
                return true;
            }
        }
        return false;
    }

    /** Combines the active modifiers into one and counts their time down; the ones that run out are removed. */
    private VehicleModifier combineActiveModifiers() {
        double maxSpeedFactor = 1.0;
        double accelerationFactor = 1.0;
        double brakeFactor = 1.0;
        double gripFactor = 1.0;
        for (int i = activeModifiers.size() - 1; i >= 0; i--) {
            ActiveModifier active = activeModifiers.get(i);
            maxSpeedFactor *= active.modifier().maxSpeedFactor();
            accelerationFactor *= active.modifier().accelerationFactor();
            brakeFactor *= active.modifier().brakeFactor();
            gripFactor *= active.modifier().gripFactor();
            if (active.remainingTicks() <= 1) {
                activeModifiers.remove(i);
            } else {
                activeModifiers.set(i, new ActiveModifier(active.modifier(), active.remainingTicks() - 1));
            }
        }
        return new VehicleModifier(maxSpeedFactor, accelerationFactor, brakeFactor, gripFactor);
    }

    /** One tick of arcade driving. Runs only where the vehicle is simulated (see isControlledByLocalInstance). */
    private void drive() {
        Vec3 motion = getDeltaMovement();

        // Terrain. Land vehicles need to be out of the water; the water vehicle needs water or ice.
        // Without traction the vehicle cannot accelerate, brake, steer or drift: it only coasts.
        boolean inWater = isInWater();
        BlockState below = getBlockStateOn();
        boolean onIce = below.is(BlockTags.ICE);
        boolean onSnowOrIce = onIce || below.is(BlockTags.SNOW);
        boolean traction = vehicleClass == VehicleClass.TIDE ? (inWater || onIce) : !inWater;

        double maxSpeed = VehiclePhysics.MAX_SPEED;
        double maxReverseSpeed = VehiclePhysics.MAX_REVERSE_SPEED;
        if (vehicleClass == VehicleClass.FROST && !onSnowOrIce) {
            maxSpeed *= VehiclePhysics.FROST_LAND_SPEED_FACTOR;
            maxReverseSpeed *= VehiclePhysics.FROST_LAND_SPEED_FACTOR;
        }

        // Modifiers (boosts, slowdowns...) added by other parts of the mod change the numbers used below.
        VehicleModifier modifiers = combineActiveModifiers();
        maxSpeed *= modifiers.maxSpeedFactor();
        maxReverseSpeed *= modifiers.maxSpeedFactor();
        double acceleration = VehiclePhysics.ACCELERATION * modifiers.accelerationFactor();
        double reverseAcceleration = VehiclePhysics.REVERSE_ACCELERATION * modifiers.accelerationFactor();
        double brakeForce = VehiclePhysics.BRAKE_FORCE * modifiers.brakeFactor();
        float gripFactor = (float) modifiers.gripFactor();

        // The movement goes where the vehicle faces, except while it slides (drift).
        if (!sliding) {
            moveYaw = getYRot();
        }
        double moveRad = Math.toRadians(moveYaw);
        // Speed along the direction of the movement; negative when going backwards.
        double speed = motion.x * -Math.sin(moveRad) + motion.z * Math.cos(moveRad);

        if (!traction) {
            speed *= inWater ? VehiclePhysics.NO_TRACTION_WATER_DRAG : VehiclePhysics.NO_TRACTION_GROUND_DRAG;
            if (Math.abs(speed) < VehiclePhysics.STOP_THRESHOLD) {
                speed = 0.0;
            }
        } else if (input.brake()) {
            if (speed > VehiclePhysics.STOP_THRESHOLD) {
                speed = Math.max(0.0, speed - brakeForce);
            } else {
                speed = Math.max(-maxReverseSpeed, speed - reverseAcceleration);
            }
        } else if (input.accelerate()) {
            if (speed < 0.0) {
                speed = Math.min(0.0, speed + brakeForce);
            } else if (speed < maxSpeed) {
                speed = Math.min(maxSpeed, speed + acceleration);
            } else {
                speed = Math.max(maxSpeed, speed - VehiclePhysics.OVERSPEED_DECELERATION);
            }
        } else {
            speed *= VehiclePhysics.COAST_DRAG;
            if (Math.abs(speed) < VehiclePhysics.STOP_THRESHOLD) {
                speed = 0.0;
            }
        }

        // Drift: starts when the key is pressed on the ground, at speed and while steering; ends when it is released.
        boolean startedDrift = false;
        if (drifting) {
            if (!traction || !input.drift() || Math.abs(speed) < VehiclePhysics.DRIFT_END_SPEED) {
                drifting = false;
            }
        } else if (traction && input.drift() && onGround() && speed > VehiclePhysics.DRIFT_MIN_SPEED
                && Math.abs(input.steer()) > VehiclePhysics.DRIFT_START_STEER) {
            drifting = true;
            startedDrift = true;
            driftDirection = input.steer() > 0.0F ? 1 : -1;
        }
        if (drifting) {
            speed *= VehiclePhysics.DRIFT_DRAG;
        }

        // Steering. While drifting it keeps the direction of the drift: steering into it tightens the turn and
        // steering against it only widens it.
        float steerTarget = traction ? input.steer() : 0.0F;
        if (drifting) {
            // -1 = steering against the drift, 0 = steering released, 1 = steering into the drift.
            float intoDrift = input.steer() * driftDirection;
            float steerFraction;
            if (intoDrift >= 0.0F) {
                steerFraction = VehiclePhysics.DRIFT_STEER_NEUTRAL
                        + (VehiclePhysics.DRIFT_STEER_INTO - VehiclePhysics.DRIFT_STEER_NEUTRAL) * intoDrift;
            } else {
                steerFraction = VehiclePhysics.DRIFT_STEER_NEUTRAL
                        + (VehiclePhysics.DRIFT_STEER_NEUTRAL - VehiclePhysics.DRIFT_STEER_AGAINST) * intoDrift;
            }
            steerTarget = driftDirection * steerFraction;
        }
        steerState = VehiclePhysics.approach(steerState, steerTarget, input.steerResponse());

        // Bicycle model: the turning rate comes from the speed and the angle of the front wheels.
        double yawRate = 0.0;
        if (traction) {
            double steerAngle = steerState * VehiclePhysics.MAX_STEER_ANGLE
                    / (1.0 + Math.abs(speed) * VehiclePhysics.STEER_SPEED_FALLOFF);
            yawRate = speed / VehiclePhysics.WHEELBASE * Math.tan(steerAngle);
            if (drifting) {
                yawRate *= VehiclePhysics.DRIFT_YAW_BOOST;
            }
        }
        float oldYaw = getYRot();
        float newYaw = Mth.wrapDegrees(oldYaw + (float) Math.toDegrees(yawRate));
        setYRot(newYaw);
        setXRot(0.0F);

        // With key steering the camera of the driver turns with the vehicle; with mouse steering it stays free.
        LivingEntity driver = getControllingPassenger();
        float yawChange = Mth.wrapDegrees(newYaw - oldYaw);
        if (driver != null && input.cameraFollows() && yawChange != 0.0F) {
            driver.setYRot(driver.getYRot() + yawChange);
            driver.yRotO += yawChange;
        }

        // Grip: how fast the movement catches up with where the vehicle faces.
        float slip = Mth.wrapDegrees(newYaw - moveYaw);
        if ((drifting || sliding) && Math.abs(slip) > VehiclePhysics.DRIFT_SPIN_OUT_DEGREES) {
            // Spun out: the drift is lost and the vehicle is left going backwards (only the part of the speed
            // that points opposite to where it faces is kept, up to the reverse top speed).
            drifting = false;
            sliding = false;
            moveYaw = newYaw;
            speed = Math.max(-maxReverseSpeed, speed * Math.cos(Math.toRadians(slip)));
        } else if (drifting) {
            sliding = true;
            moveYaw = Mth.wrapDegrees(moveYaw + Mth.wrapDegrees(newYaw - moveYaw) * Mth.clamp(VehiclePhysics.DRIFT_GRIP * gripFactor, 0.0F, 1.0F));
        } else if (sliding) {
            moveYaw = Mth.wrapDegrees(moveYaw + Mth.wrapDegrees(newYaw - moveYaw) * Mth.clamp(VehiclePhysics.DRIFT_RECOVER_GRIP * gripFactor, 0.0F, 1.0F));
            if (Math.abs(Mth.wrapDegrees(newYaw - moveYaw)) < VehiclePhysics.SLIP_END_DEGREES) {
                sliding = false;
                moveYaw = newYaw;
            }
        } else {
            moveYaw = newYaw;
        }

        // Vertical movement: gravity, or floating when the hitbox is in water (any class floats).
        double waterHeight = getFluidHeight(FluidTags.WATER);
        double verticalSpeed;
        if (waterHeight > 0.0) {
            verticalSpeed = (motion.y + (waterHeight - VehiclePhysics.FLOAT_DEPTH) * VehiclePhysics.BUOYANCY)
                    * VehiclePhysics.WATER_VERTICAL_DRAG;
        } else {
            verticalSpeed = (motion.y - VehiclePhysics.GRAVITY) * VehiclePhysics.VERTICAL_DRAG;
        }
        // Land vehicles hop when the drift starts.
        if (startedDrift && vehicleClass.hopsWhenDrifting()) {
            verticalSpeed = VehiclePhysics.DRIFT_HOP_SPEED;
        }

        moveRad = Math.toRadians(moveYaw);
        setDeltaMovement(-Math.sin(moveRad) * speed, verticalSpeed, Math.cos(moveRad) * speed);
        move(MoverType.SELF, getDeltaMovement());
    }

    /**
     * Updates how much the wheels have rolled, how much the front wheels are turned and how much the vehicle leans,
     * all from the real movement, so every client gets the same result without extra packets.
     */
    private void updateAnimationState() {
        double x = getX();
        double z = getZ();
        float yaw = getYRot();

        rolledDistanceO = rolledDistance;
        visualSteerO = visualSteer;
        visualTiltO = visualTilt;
        if (animInitialized) {
            double dx = x - animLastX;
            double dz = z - animLastZ;
            observedSpeed = (float) Math.hypot(dx, dz);
            observedDx = dx;
            observedDz = dz;
            double yawRad = Math.toRadians(yaw);
            double forwardMove = dx * -Math.sin(yawRad) + dz * Math.cos(yawRad);
            rolledDistance += (float) forwardMove;

            double steerTarget = 0.0;
            if (Math.abs(forwardMove) > VehiclePhysics.MIN_VISUAL_STEER_SPEED) {
                double yawChange = Math.toRadians(Mth.wrapDegrees(yaw - animLastYaw));
                steerTarget = Mth.clamp(Math.atan(VehiclePhysics.WHEELBASE * yawChange / forwardMove),
                        -VehiclePhysics.MAX_VISUAL_STEER, VehiclePhysics.MAX_VISUAL_STEER);
            }
            visualSteer = VehiclePhysics.approach(visualSteer, (float) steerTarget, VehiclePhysics.VISUAL_STEER_RESPONSE);

            // Lean towards the side the vehicle is turning to while it slides: the movement lags behind the facing.
            observedSlip = 0.0F;
            float tiltTarget = 0.0F;
            if (Math.hypot(dx, dz) > VehiclePhysics.MIN_TILT_SPEED) {
                double movementYaw = Math.toDegrees(Math.atan2(-dx, dz));
                if (forwardMove < 0.0) {
                    movementYaw += 180.0;
                }
                float slip = Mth.wrapDegrees((float) movementYaw - yaw);
                observedSlip = slip;
                float beyondDeadZone = Math.max(0.0F, Math.abs(slip) - VehiclePhysics.TILT_DEAD_ZONE_DEGREES);
                float slipRange = VehiclePhysics.TILT_FULL_SLIP_DEGREES - VehiclePhysics.TILT_DEAD_ZONE_DEGREES;
                tiltTarget = -Math.signum(slip) * Mth.clamp(beyondDeadZone / slipRange, 0.0F, 1.0F)
                        * VehiclePhysics.MAX_TILT_DEGREES;
            }
            visualTilt = VehiclePhysics.approach(visualTilt, tiltTarget, VehiclePhysics.TILT_RESPONSE_DEGREES);
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
