package com.whiuk.philip.mud.server;

enum LocationType {
    SURFACE_FLAT("on an area of", "relatively flat ground"),
    SURFACE_ROLLING("amongst", "gentle rolling hills"),
    SURFACE_MOUNTAIN_BASE("at the", "foot of a mountain"),
    SURFACE_MOUNTAIN_FACE("on the", "face of a mountain"),
    SURFACE_MOUNTAIN_SUMMIT("at the", "top of a mountain"),
    UNDERGROUND_SOLID_ROCK("amongst ", "solid rock"),
    UNDERGROUND_CAVE("in", "a cave"),
    UNDERGROUND_TUNNEL("in", "a tunnel"),
    UNDERGROUND_CHASM("on", "the ledge of an underground chasm");

    final String inPrefix;
    final String description;

    LocationType(String inPrefix, String description) {
        this.inPrefix = inPrefix;
        this.description = description;
    }
}