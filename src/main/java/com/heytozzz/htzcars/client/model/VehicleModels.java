package com.heytozzz.htzcars.client.model;

import com.heytozzz.htzcars.HtzCars;
import com.heytozzz.htzcars.vehicle.VehicleClass;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The list of vehicle models. Client-only code.
 *
 * HOW TO ADD A MODEL
 * 1. In Blockbench (format "Modded Entity") name the groups like this. Everything goes inside a group called "body".
 *    Inside "body" (all of them are optional, a model without one just does not animate it):
 *    - wheel_bl, wheel_br (rear wheels) and wheel_fl, wheel_fr (front wheels): the pivot must be at the center of the
 *      wheel, because they roll around the X axis. The front ones also turn when steering.
 *    - steering_wheel > steering_wheel_tilt > steering_wheel_disc: the tilt group holds the tilt of the steering wheel
 *      and the disc group (rotation 0, pivot at the center of the disc) is the part that turns.
 *    - exhaust (all the pieces of the exhaust) and exhaust_tip (an empty group, rotation 0, pivot at the center of the
 *      mouth of the pipe): this is where the smoke and the sparks come out. With two exhausts: exhaust_l, exhaust_r
 *      and exhaust_tip_l, exhaust_tip_r. With more: exhaust_1, exhaust_2... and exhaust_tip_1, exhaust_tip_2...
 *      (the tip groups are the ones the code looks for, so each one must be a direct child of "body").
 * 2. Export it as Java and create a file in this folder (client/model) named after the model, for example
 *    MuscleModel.java, with this content (the imports and the empty private constructor are always the same, and the
 *    method is pasted from the export as it is, without the generics, the fields or the constructor):
 *
 *        package com.heytozzz.htzcars.client.model;
 *        import net.minecraft.client.model.geom.PartPose;
 *        import net.minecraft.client.model.geom.builders.CubeDeformation;
 *        import net.minecraft.client.model.geom.builders.CubeListBuilder;
 *        import net.minecraft.client.model.geom.builders.LayerDefinition;
 *        import net.minecraft.client.model.geom.builders.MeshDefinition;
 *        import net.minecraft.client.model.geom.builders.PartDefinition;
 *        public final class MuscleModel {
 *            private MuscleModel() {}
 *            public static LayerDefinition createBodyLayer() { ...pasted from the export... }
 *        }
 *
 * 3. Put the textures in src/main/resources/assets/htzcars/textures/entity/vehicle/ as <name>.png (white base, it gets
 *    tinted) and <name>_details.png (drawn on top, never tinted). <name> is the name used in the line below: only
 *    lowercase letters, digits and underscores. Both must have the size of the texture of the Blockbench project.
 * 4. Add a line to the static block below. The first model of a class is the index 0, the next one the index 1...
 *    The two last numbers are the radius of the rear and of the front wheels in model pixels (to roll them the right
 *    amount). Using the same name for several classes shares the same model.
 */
public final class VehicleModels {

    /** One model of a vehicle class. */
    public record Entry(String name, Supplier<LayerDefinition> layer, float backWheelRadiusPixels, float frontWheelRadiusPixels) {

        public ModelLayerLocation layerLocation() {
            return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HtzCars.MOD_ID, name), "main");
        }

        /** White base texture: it is tinted with the vehicle color. */
        public ResourceLocation texture() {
            return ResourceLocation.fromNamespaceAndPath(HtzCars.MOD_ID, "textures/entity/vehicle/" + name + ".png");
        }

        /** Details texture: drawn on top of the base texture, never tinted. */
        public ResourceLocation detailsTexture() {
            return ResourceLocation.fromNamespaceAndPath(HtzCars.MOD_ID, "textures/entity/vehicle/" + name + "_details.png");
        }
    }

    private static final Map<VehicleClass, List<Entry>> MODELS = new EnumMap<>(VehicleClass.class);

    static {
        // Temporary: the go-kart is used by every class until the final models are in.
        add(VehicleClass.TRAIL, "gokart", GokartModel::createBodyLayer, 4.0F, 3.0F);
        add(VehicleClass.CIRCUIT, "gokart", GokartModel::createBodyLayer, 4.0F, 3.0F);
        add(VehicleClass.FROST, "gokart", GokartModel::createBodyLayer, 4.0F, 3.0F);
        add(VehicleClass.TIDE, "gokart", GokartModel::createBodyLayer, 4.0F, 3.0F);

        for (VehicleClass vehicleClass : VehicleClass.values()) {
            if (MODELS.getOrDefault(vehicleClass, List.of()).isEmpty()) {
                throw new IllegalStateException("The vehicle class " + vehicleClass + " has no model in VehicleModels");
            }
        }
    }

    private VehicleModels() {
    }

    private static void add(VehicleClass vehicleClass, String name, Supplier<LayerDefinition> layer,
                            float backWheelRadiusPixels, float frontWheelRadiusPixels) {
        if (backWheelRadiusPixels <= 0.0F || frontWheelRadiusPixels <= 0.0F) {
            throw new IllegalArgumentException("The wheel radius of the model " + name + " must be above 0");
        }
        MODELS.computeIfAbsent(vehicleClass, key -> new ArrayList<>())
                .add(new Entry(name, layer, backWheelRadiusPixels, frontWheelRadiusPixels));
    }

    /** The models of a class, in index order. */
    public static List<Entry> of(VehicleClass vehicleClass) {
        return MODELS.get(vehicleClass);
    }

    /** The model with that index; if the index does not exist (a model was removed) the first one is used. */
    public static Entry get(VehicleClass vehicleClass, int index) {
        List<Entry> entries = MODELS.get(vehicleClass);
        return index >= 0 && index < entries.size() ? entries.get(index) : entries.get(0);
    }

    /** One entry for each different model name, which is what has to be registered as a model layer. */
    public static Collection<Entry> distinctModels() {
        Map<String, Entry> byName = new LinkedHashMap<>();
        for (List<Entry> entries : MODELS.values()) {
            for (Entry entry : entries) {
                byName.putIfAbsent(entry.name(), entry);
            }
        }
        return byName.values();
    }
}
