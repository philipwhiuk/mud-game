package com.whiuk.philip.mud.server;

import java.util.HashMap;
import java.util.Map;

abstract class Thing {
    protected final ThingType type;
    private Map<String, Map<String, Recipe>> recipes = new HashMap<>();

    Thing(ThingType type) {
        this.type = type;
    }

    String id() {
        return type.id;
    }

    abstract String getDescription();

    abstract void tick(Chunk chunk, Location location);

    boolean canTake() {
        return false;
    }

    Item handleTake(Location location) {
        return null;
    }

    boolean canReceive() {
        return false;
    }

    void handleReceive(Item item) {
    }
}