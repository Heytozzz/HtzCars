package com.heytozzz.htzcars.vehicle;

/**
 * The classes of vehicles. The id is used for registry names, so changing it renames the entity type and the item.
 * hopsWhenDrifting: land vehicles do a small hop when they start to drift; water vehicles do not.
 */
public enum VehicleClass {
    /** Off-road vehicle. */
    TRAIL("trail", true),
    /** Track vehicle. */
    CIRCUIT("circuit", true),
    /** Snow vehicle. */
    FROST("frost", true),
    /** Water vehicle. */
    TIDE("tide", false);

    private final String id;
    private final boolean hopsWhenDrifting;

    VehicleClass(String id, boolean hopsWhenDrifting) {
        this.id = id;
        this.hopsWhenDrifting = hopsWhenDrifting;
    }

    public String getId() {
        return id;
    }

    public boolean hopsWhenDrifting() {
        return hopsWhenDrifting;
    }
}
