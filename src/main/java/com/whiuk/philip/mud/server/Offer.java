package com.whiuk.philip.mud.server;

class Offer {
    final int id;
    private final String give;
    private final String take;
    private final PlayerCharacter originator;

    public Offer(int id, String give, String take, PlayerCharacter originator) {
        this.id = id;
        this.give = give;
        this.take = take;
        this.originator = originator;
    }
}