package com.whiuk.philip.mud.server;

enum Stance {
    STANDING("standing"), CROUCHING("crouching"), PRONE("prone");

    final String description;

    Stance(String description) {
        this.description = description;
    }
}