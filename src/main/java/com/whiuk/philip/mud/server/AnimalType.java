package com.whiuk.philip.mud.server;

import static com.whiuk.philip.mud.server.FeedingType.CARNIVORE;
import static com.whiuk.philip.mud.server.FeedingType.HERBIVORE;

class AnimalType extends NPCType {

    final FeedingType feedingType;

    AnimalType(String id, String description, int maxHealth, FeedingType feedingType) {
        super(id, description, maxHealth);
        this.feedingType = feedingType;
    }

    @Override
    public Thing create() {
        return new Animal(this);
    }
}