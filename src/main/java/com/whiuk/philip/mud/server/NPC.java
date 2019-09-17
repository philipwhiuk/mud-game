package com.whiuk.philip.mud.server;

import java.util.Collections;

abstract class NPCType extends ThingType {
    final String description;
    final int maxHealth;

    public NPCType(String id, String description, int maxHealth) {
        super(id, Collections.emptyMap());
        this.description = description;
        this.maxHealth = maxHealth;
    }
}

abstract class NPC extends Thing {
    private int health;
    private NPCType npcType;

    NPC(NPCType npcType) {
        super(npcType);
        this.npcType = npcType;
        this.health = npcType.maxHealth;
    }

    @Override
    public String getDescription() {
        return id() + " - " + npcType.description + " " + health + "/" + npcType.maxHealth;
    }

    @Override
    public void tick(Chunk chunk, Location location) {

    }
}