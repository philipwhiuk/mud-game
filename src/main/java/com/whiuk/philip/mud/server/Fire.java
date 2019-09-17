package com.whiuk.philip.mud.server;

import java.util.Collections;

class FireType extends ThingType {

    public FireType(String id) {
        super(id, Collections.emptyMap());
    }

    @Override
    public Thing create() {
        return new Fire(this);
    }
}

class Fire extends Thing {

    private int fuel;

    Fire(FireType fireType) {
        super(fireType);
        fuel = 10;
    }

    @Override
    public String getDescription() {
        if (fuel > 100) {
            return id() + " - A fire, burning brightly - it will last for a while";
        }
        if (fuel > 10) {
            return id() + " - A fire, burning gently";
        }
        return id() + " - A fire, it will not last long without more fuel";
    }

    @Override
    public void tick(Chunk chunk, Location location) {
        fuel -= 1;
        if (fuel == 0) {
            chunk.broadcast(location, "The fire sputters out and dies.");
            location.remove(this);
        }
    }
}