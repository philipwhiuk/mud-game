package com.whiuk.philip.mud.server;

public class Equipment extends Item {
    private Slot slot;

    Equipment(ItemType itemType) {
        super(itemType);
    }

    public Slot getSlot() {
        return ((EquipmentType) itemType).slot;
    }
}
