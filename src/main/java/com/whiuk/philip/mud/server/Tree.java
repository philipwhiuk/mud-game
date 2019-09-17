package com.whiuk.philip.mud.server;

import java.util.Collections;

class TreeType extends ThingType {

    TreeType(String id) {
        super(id, Collections.singletonMap("AXE", Collections.singletonMap("FELLED-TREE", "CHOP-TREE")));
    }

    @Override
    public Thing create() {
        return new Tree(this);
    }
}

public class Tree extends Thing {

    Tree(TreeType type) {
        super(type);
    }

    //TODO: Tree growth
    //TODO: Tree log types

    @Override
    String getDescription() {
        return "A small "+type.id+" tree";
    }

    @Override
    void tick(Chunk chunk, Location location) {

    }
}
