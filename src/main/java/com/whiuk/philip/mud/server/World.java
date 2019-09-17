package com.whiuk.philip.mud.server;

import java.util.ArrayList;
import java.util.List;

class World {
    @SuppressWarnings("unused")
    Chunk[][] map;
    private List<PlayerCharacter> players = new ArrayList<>();

    World(int x, int y) {
        map = new Chunk[x][y];
    }

    public void playerJoined(PlayerCharacter character) {
        players.add(character);
        character.sendMessage(String.format("%1$s %2$s. There %3$s %4$d %5$s online.",
                character.firstLogin ? "Welcome" : "Welcome back",
                character.name,
                players.size() > 1 ? "are" : "is",
                players.size(),
                players.size() > 1 ? "players" : "player"));
        Chunk chunk = character.chunk;
        chunk.add(character);
    }

    public void playerLeaving(PlayerCharacter character) {
        players.remove(character);
        character.chunk.remove(character);
    }

    public void handleLook(PlayerCharacter character, String direction, Chunk chunk) {
        //TODO: Look at other chunks
        character.sendMessage("Unexplored land");
    }

    public void handleMove(PlayerCharacter character, String direction, Chunk chunk) {
        //TODO: Move to other chunks
        character.sendMessage("Can't move there right now");
    }
}