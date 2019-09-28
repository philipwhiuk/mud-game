package com.whiuk.philip.mud.server;

import java.util.Map;

class ItemType extends ThingType {

    public final String name;

    ItemType(String id, String name, Map<String, Map<String, String>> recipes) {
        super(id, recipes);
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        this.name = name;
    }

    public Item create() {
        return new Item(this);
    }
}

class Item extends Thing {
    protected ItemType itemType;
    public String creator = null;
    public float quality = 1.0f;

    Item(ItemType itemType) {
        super(itemType);
        this.itemType = itemType;
    }

    String getName() {
        return itemType.name;
    }

    @Override
    String getDescription() {
        return itemType.name;
    }

    @Override
    void tick(Chunk chunk, Location location) {

    }
}