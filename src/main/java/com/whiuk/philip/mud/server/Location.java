package com.whiuk.philip.mud.server;

import java.util.*;
import java.util.stream.Collectors;

class Location {
    LocationType locationType;
    public final int x;
    public final int y;
    public final int z;
    final List<Thing> things = new ArrayList<>();

    Location(int x, int y, int z, LocationType locationType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.locationType = locationType;
    }

    public String describeStuff() {
        if (things.size() == 0) {
            return "";
        } else {
            String description = " There's the following here: ";
            return description + things.stream().map(Thing::id).collect(Collectors.joining(","));
        }
    }

    public void tick(Chunk chunk) {
        things.forEach(thing -> thing.tick(chunk, this));
    }

    public void remove(Thing thing) {
        things.remove(thing);
    }

    public void add(Thing thing) {
        things.add(thing);
    }

    public boolean has(String targetId) {
        return things.stream().anyMatch(t -> t.id().equals(targetId));
    }

    public Optional<Thing> get(String targetId) {
        return things.stream().filter(t -> t.id().equals(targetId)).findFirst();
    }
}