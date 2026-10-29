package com.heytozzz.htzcars.client.model;

import com.heytozzz.htzcars.vehicle.VehicleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A baked vehicle model. It works with any model made following the names in VehicleModels: the moving parts
 * (wheels, steering wheel) and the exhaust tips are looked up by name and are optional. Client-only code.
 */
public class VehicleModel extends EntityModel<VehicleEntity> {

    /** Height in blocks at which the model is moved up so it stands on the ground. The renderers use this same value. */
    public static final float MODEL_HEIGHT_OFFSET = 1.501F;

    /** The steering wheel turns this many times more than the front wheels. */
    private static final float STEERING_WHEEL_RATIO = 3.0F;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final int MAX_NUMBERED_EXHAUSTS = 16;

    private final ModelPart root;
    private final VehicleModels.Entry entry;
    private final float backWheelRadius;
    private final float frontWheelRadius;
    private final List<ModelPart> backWheels = new ArrayList<>();
    private final List<ModelPart> frontWheels = new ArrayList<>();
    private final ModelPart steeringWheelDisc;
    private final List<ModelPart> exhaustTips = new ArrayList<>();
    /** Position of the "body" group, which is the parent of the exhaust tips (in model pixels). */
    private final float bodyX;
    private final float bodyY;
    private final float bodyZ;

    public VehicleModel(ModelPart root, VehicleModels.Entry entry) {
        this.root = root;
        this.entry = entry;
        this.backWheelRadius = entry.backWheelRadiusPixels() * PIXEL;
        this.frontWheelRadius = entry.frontWheelRadiusPixels() * PIXEL;

        ModelPart body = root.hasChild("body") ? root.getChild("body") : null;
        ModelPart parent = body != null ? body : root;
        this.bodyX = body != null ? body.x : 0.0F;
        this.bodyY = body != null ? body.y : 0.0F;
        this.bodyZ = body != null ? body.z : 0.0F;

        addIfPresent(backWheels, parent, "wheel_bl");
        addIfPresent(backWheels, parent, "wheel_br");
        addIfPresent(frontWheels, parent, "wheel_fl");
        addIfPresent(frontWheels, parent, "wheel_fr");

        ModelPart disc = null;
        if (parent.hasChild("steering_wheel")) {
            ModelPart steeringWheel = parent.getChild("steering_wheel");
            if (steeringWheel.hasChild("steering_wheel_tilt")) {
                ModelPart tilt = steeringWheel.getChild("steering_wheel_tilt");
                if (tilt.hasChild("steering_wheel_disc")) {
                    disc = tilt.getChild("steering_wheel_disc");
                }
            }
        }
        this.steeringWheelDisc = disc;

        addIfPresent(exhaustTips, parent, "exhaust_tip");
        addIfPresent(exhaustTips, parent, "exhaust_tip_l");
        addIfPresent(exhaustTips, parent, "exhaust_tip_r");
        for (int i = 1; i <= MAX_NUMBERED_EXHAUSTS; i++) {
            addIfPresent(exhaustTips, parent, "exhaust_tip_" + i);
        }
    }

    private static void addIfPresent(List<ModelPart> list, ModelPart parent, String name) {
        if (parent.hasChild(name)) {
            list.add(parent.getChild(name));
        }
    }

    public ResourceLocation getTexture() {
        return entry.texture();
    }

    public ResourceLocation getDetailsTexture() {
        return entry.detailsTexture();
    }

    /**
     * Sets the pose of the moving parts. rolledDistance: blocks rolled so far; steerAngle: angle of the front wheels
     * in radians (positive = right). If a wheel or the steering wheel turns the wrong way, flip its sign here.
     */
    public void setAngles(float rolledDistance, float steerAngle) {
        float backRoll = rolledDistance / backWheelRadius;
        float frontRoll = rolledDistance / frontWheelRadius;
        for (ModelPart wheel : backWheels) {
            wheel.xRot = backRoll;
        }
        for (ModelPart wheel : frontWheels) {
            wheel.xRot = frontRoll;
            wheel.yRot = steerAngle;
        }
        if (steeringWheelDisc != null) {
            steeringWheelDisc.zRot = -steerAngle * STEERING_WHEEL_RATIO;
        }
    }

    /** How many exhaust tips the model has (0 if it has none). */
    public int getExhaustTipCount() {
        return exhaustTips.size();
    }

    /**
     * Where an exhaust tip is, in blocks, measured from the position of the vehicle in the world. yawDegrees is the
     * yaw of the vehicle and tiltDegrees its visual lean (the same values the renderer uses), so the point follows the
     * model exactly. The tip groups must be direct children of "body", with no rotation, and "body" with no rotation.
     */
    public Vec3 getExhaustTipOffset(int index, float yawDegrees, float tiltDegrees) {
        ModelPart tip = exhaustTips.get(index);
        // Same transformation as the renderer: axes flipped and the model moved up.
        double x = -(bodyX + tip.x) * PIXEL;
        double y = MODEL_HEIGHT_OFFSET - (bodyY + tip.y) * PIXEL;
        double z = (bodyZ + tip.z) * PIXEL;

        // The lean the renderer applies (around the front-to-back axis).
        double lean = Math.toRadians(-tiltDegrees);
        double leanedX = x * Math.cos(lean) - y * Math.sin(lean);
        double leanedY = x * Math.sin(lean) + y * Math.cos(lean);

        // The turn the renderer applies (around the vertical axis).
        double turn = Math.toRadians(180.0 - yawDegrees);
        return new Vec3(leanedX * Math.cos(turn) + z * Math.sin(turn),
                leanedY,
                -leanedX * Math.sin(turn) + z * Math.cos(turn));
    }

    @Override
    public void setupAnim(VehicleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // The moving parts are posed with setAngles.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, consumer, packedLight, packedOverlay, color);
    }
}
