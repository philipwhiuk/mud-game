package com.whiuk.philip.mud.server;

import java.util.Collections;

class PlantType extends ThingType {
    public static final PlantType GRASS = new PlantType("GRASS", "It's green and growing");;
    final String description;

    PlantType(String id, String description) {
        super(id, Collections.emptyMap());
        this.description = description;
    }

    @Override
    public Thing create() {
        return new Plant(this);
    }
}

class Plant extends Thing {
    private final PlantType plantType;

    Plant(PlantType type) {
        super(type);
        this.plantType = type;
    }

    @Override
    public String getDescription() {
        return plantType.description;
    }

    @Override
    public void tick(Chunk chunk, Location location) {

    }
}