package com.whiuk.philip.mud.server;

import java.util.HashMap;
import java.util.Map;

abstract class ThingType {
    public final String id;
    private Map<String,Map<String,String>> recipes;

    public ThingType(String id, Map<String,Map<String,String>> recipes) {
        this.id = id;
        this.recipes = new HashMap<>(recipes);
    }

    public Map<String, Map<String, String>> getRecipes() {
        return recipes;
    }

    public abstract Thing create();
}
