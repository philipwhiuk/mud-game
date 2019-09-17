package com.whiuk.philip.mud.server;

import java.util.Map;

class ObjectType extends ThingType {
    public final String description;

    public ObjectType(String id, String description, Map<String, Map<String, String>> recipes) {
        super(id, recipes);
        this.description = description;
    }

    @Override
    public Thing create() {
        return new GameObject(this);
    }
}

public class GameObject extends Thing {
    private final ObjectType objectType;

    public GameObject(ObjectType objectType) {
        super(objectType);
        this.objectType = objectType;
    }

    @Override
    String getDescription() {
        return objectType.description;
    }

    @Override
    void tick(Chunk chunk, Location location) {

    }
}
