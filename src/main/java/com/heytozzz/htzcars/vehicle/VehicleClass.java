package com.heytozzz.htzcars.vehicle;

/**
 * The classes of vehicles. The id is used for registry names, so changing it renames the entity type and the item.
 */
public enum VehicleClass {
    /** Off-road vehicle. */
    TRAIL("trail"),
    /** Track vehicle. */
    CIRCUIT("circuit"),
    /** Snow vehicle. */
    FROST("frost"),
    /** Water vehicle. */
    TIDE("tide");

    private final String id;

    VehicleClass(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
