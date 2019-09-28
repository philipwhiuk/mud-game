package com.whiuk.philip.mud.server;

import java.util.Map;

public class EquipmentType extends ItemType {
    public Slot slot;

    EquipmentType(String id, String name, Map<String, Map<String, String>> recipes, Slot slot) {
        super(id, name, recipes);
        this.slot = slot;
    }

    public Equipment create() {
        return new Equipment(this);
    }
}
