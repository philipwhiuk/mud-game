package com.whiuk.philip.mud.server;

import java.util.Collections;

public class EquipmentType extends ItemType {
    public Slot slot;

    EquipmentType(String id, String name, Slot slot) {
        super(id, name, Collections.emptyMap());
        this.slot = slot;
    }
}
