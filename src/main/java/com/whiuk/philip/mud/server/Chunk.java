package com.whiuk.philip.mud.server;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

class Chunk {
    @SuppressWarnings("unused")
    private final ScheduledFuture<?> tickFuture;
    private final ThingStore thingStore;
    private final RecipeStore recipeStore;
    private final World world;
    private List<PlayerCharacter> characters = new ArrayList<>();
    private Biome biome;
    Location[][][] map;
    private ScheduledExecutorService taskQueue = Executors.newSingleThreadScheduledExecutor();

    Chunk(ThingStore thingStore, RecipeStore recipeStore, World world, Biome biome, Location[][][] map) {
        this.thingStore = thingStore;
        this.recipeStore = recipeStore;
        this.world = world;
        this.biome = biome;
        this.map = map;
        this.tickFuture = taskQueue.scheduleAtFixedRate(this::tick,
                600, 600, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        switch (biome) {
            case PLAINS:
            case ROLLING_HILLS:
                Location location = randomLocation();
                switch (location.locationType) {
                    case SURFACE_FLAT:
                    case SURFACE_ROLLING:
                        if (!location.has("GRASS")) {
                            location.add(thingStore.get("GRASS").create());
                        } else if (!location.has("SHEEP")) {
                            location.add(thingStore.get("SHEEP").create());
                        } else if (!location.has("WOLF")) {
                            location.add(thingStore.get("WOLF").create());
                        }
                }
        }
        for (Location[][] xLocation : map) {
            for (Location[] xyLocation : xLocation) {
                for (Location location : xyLocation) {
                    location.tick(this);
                }
            }
        }
    }

    void shutdown() {
        tickFuture.cancel(false);
    }

    private Location randomLocation() {
        Random random = new Random();
        return map[random.nextInt(3)][random.nextInt(3)][random.nextInt(3)];
    }

    void add(PlayerCharacter character) {
        characters.forEach(existingChar -> existingChar.sendMessage(character.name + " has logged in"));
        characters.add(character);
        character.sendMessage(generateEntryMessage(character));
    }

    void remove(PlayerCharacter character) {
        characters.remove(character);
        characters.forEach(remainingChar -> remainingChar.sendMessage(character.name + " has logged out"));
    }

    private String generateEntryMessage(PlayerCharacter character) {
        int characterCount = characters.size() -1;
        String charactersAround;
        if (characterCount == 0) {
            charactersAround = "no other players";
        } else if (characterCount == 1) {
            charactersAround = "one other player";
        } else {
            charactersAround = characterCount + " other players";
        }
        return String.format("You're %1$s %2$s %3$s. There's %4$s nearby.%5$s",
                character.stance.description,
                character.location.locationType.inPrefix,
                character.location.locationType.description,
                charactersAround,
                character.location.describeStuff());
    }

    public void handleLook(PlayerCharacter character) {
        character.sendMessage(generateEntryMessage(character));
    }

    public void handleLook(PlayerCharacter character, String direction) {
        Location currentLocation = character.location;
        switch (Direction.valueOf(direction)) {
            case NORTH:
                if (currentLocation.y == 0) {
                    world.handleLook(character, direction, this);
                } else {
                    Location lookingAt = map[currentLocation.x][currentLocation.y - 1][currentLocation.z];
                    character.sendMessage(String.format("To the north is %1$s",
                            lookingAt.locationType.description));
                }
                break;
            case WEST:
                if (currentLocation.x == 0) {
                    world.handleLook(character, direction, this);
                } else {
                    Location lookingAt = map[currentLocation.x - 1][currentLocation.y][currentLocation.z];
                    character.sendMessage(String.format("To the north is %1$s",
                            lookingAt.locationType.description));
                }
                break;
            case SOUTH:
                if (currentLocation.y == 2) {
                    world.handleLook(character, direction, this);
                } else {
                    Location lookingAt = map[currentLocation.x][currentLocation.y + 1][currentLocation.z];
                    character.sendMessage(String.format("To the south is %1$s",
                            lookingAt.locationType.description));
                }
                break;
            case EAST:
                if (currentLocation.x == 2) {
                    world.handleLook(character, direction, this);
                } else {
                    Location lookingAt = map[currentLocation.x + 1][currentLocation.y][currentLocation.z];
                    character.sendMessage(String.format("To the east is %1$s",
                            lookingAt.locationType.description));
                }
                break;
            default:
                character.sendMessage("Unknown direction: " + direction);
        }
    }

    public void handleMove(PlayerCharacter character, String direction) {
        Location currentLocation = character.location;
        switch (Direction.valueOf(direction)) {
            case NORTH:
                if (currentLocation.y == 0) {
                    world.handleMove(character, direction, this);
                } else {
                    Location newLocation = map[currentLocation.x][currentLocation.y - 1][currentLocation.z];
                    move(character, currentLocation, newLocation);
                }
                break;
            case WEST:
                if (currentLocation.x == 0) {
                    world.handleMove(character, direction, this);
                } else {
                    Location newLocation = map[currentLocation.x - 1][currentLocation.y][currentLocation.z];
                    move(character, currentLocation, newLocation);
                }
                break;
            case SOUTH:
                if (currentLocation.y == 2) {
                    world.handleMove(character, direction, this);
                } else {
                    Location newLocation = map[currentLocation.x][currentLocation.y + 1][currentLocation.z];
                    move(character, currentLocation, newLocation);
                }
                break;
            case EAST:
                if (currentLocation.x == 2) {
                    world.handleMove(character, direction, this);
                } else {
                    Location newLocation = map[currentLocation.x + 1][currentLocation.y][currentLocation.z];
                    move(character, currentLocation, newLocation);
                }
                break;
            default:
                character.sendMessage("Unknown direction: " + direction);
        }
    }

    private void move(PlayerCharacter character, Location oldLocation, Location newLocation) {
        if (!character.canMove()) {
            character.sendMessage("You can't move right now!");
            return;
        }
        character.sendMessage("Moving...");
        character.startTask(taskQueue, () -> {
            if (!character.canMove()) {
                character.sendMessage("You can't move right now!");
            } else {
                performMove(character, oldLocation, newLocation);
            }
        }, 1000, "You stop moving.");
    }

    private void performMove(PlayerCharacter mover, Location oldLocation, Location newLocation) {
        characters.forEach(character -> {
            if (character != mover && character.location == oldLocation) {
                character.sendMessage(mover.name + " has left");
            } else if (character != mover && character.location == newLocation) {
                character.sendMessage(mover.name + " has arrived");
            }
        });
        mover.location = newLocation;
        mover.sendMessage(generateEntryMessage(mover));
    }

    public void handleUse(PlayerCharacter character, String itemId, String targetId, String recipe) {
        if (!character.hasItem(itemId)) {
            character.sendMessage("You don't have a " + itemId);
        } else {
            Item item = character.getItem(itemId);
            if (character.hasItem(targetId)) {
                useItemOnThing(character, item, character.getItem(targetId), recipe);
            } else {
                Optional<Thing> thing = character.location.get(targetId);
                if (thing.isPresent()) {
                    useItemOnThing(character, item, thing.get(), recipe);
                } else {
                    character.sendMessage("There's no " + targetId + " nearby");
                }
            }
        }
    }

    private void useItemOnThing(PlayerCharacter character, Item item, Thing target, String recipeResultId) {
        Map<String, String> recipes;
        if (item.type.getRecipes().containsKey(target.id())) {
            recipes = item.type.getRecipes().get(target.id());
        } else if (target.type.getRecipes().containsKey(item.id())) {
            recipes = target.type.getRecipes().get(item.id());
        } else {
            character.sendMessage("Nothing interesting happens");
            return;
        }
        String recipeId;
        if (recipes.size() == 0) {
            character.sendMessage("Nothing interesting happens");
            return;
        }
        if (recipes.size() == 1) {
            recipeId = recipes.entrySet().iterator().next().getValue();
        } else {
            recipeId = recipes.get(recipeResultId);
        }
        if (recipeId == null) {
            character.sendMessage("Nothing interesting happens");
            return;
        }
        Recipe recipe = recipeStore.get(recipeId);
        if (recipe == null) {
            System.err.println("Missing recipe in store");
            character.sendMessage("Nothing interesting happens");
            return;
        }
        recipe.scheduleTask(this, taskQueue, character, item, target);
    }

    public void handleSay(PlayerCharacter speaker, String message) {
        charactersInLocation(speaker.location).forEach(character -> {
            if (character != speaker) {
                character.sendMessage(speaker.name + ": " + message);
            }
        });
    }

    private Stream<PlayerCharacter> charactersInLocation(Location location) {
        return characters.stream().filter(character -> character.location == location);
    }

    void broadcast(Location location, String message) {
        charactersInLocation(location).forEach(character -> character.sendMessage(message));
    }

    public void handleShout(PlayerCharacter speaker, String message) {
        characters.forEach(character -> {
            if (character != speaker) {
                character.sendMessage(speaker.name + ": " + message);
            }
        });
    }

    public void handleOffer(PlayerCharacter originator, String target, String give, String take) {
        if (target.equals(originator.name)) {
            originator.sendMessage("You don't need to offer yourself something!");
        }
        Optional<PlayerCharacter> targetPlayer =
                characters.stream().filter(character -> character.name.equals(target)).findFirst();
        if (targetPlayer.isPresent()) {
            targetPlayer.get().createOfferFromPlayer(give, take, originator);
        } else {
            originator.sendMessage("That player isn't around.");
        }
    }

    public void handleOfferAccept(PlayerCharacter character, Offer offer) {
        //TODO:
    }

    public void handleOfferReject(PlayerCharacter character, Offer offer) {
        //TODO:
    }

    private Stream<PlayerCharacter> otherCharactersInLocation(PlayerCharacter character) {
        return charactersInLocation(character.location).filter(otherChar -> otherChar != character);
    }

    public void handleWho(PlayerCharacter character) {
        Stream<PlayerCharacter> otherPlayers = otherCharactersInLocation(character);
        character.sendMessage("Nearby players");
        if (otherPlayers.findAny().isPresent()) {
            otherPlayers.forEach(otherPlayer -> character.sendMessage(otherPlayer.name));
        }
    }

    public void handleGet(PlayerCharacter character, String id) {
        if (character.location.has(id)) {
            Thing thing = character.location.get(id).get();
            if (thing.canTake()) {
                character.receiveItem(thing.handleTake(character.location));
            }
        }
    }

    public void handleDrop(PlayerCharacter character, String id) {
        if (character.hasItem(id)) {
            if (character.location.has(id)) {
                Thing thing = character.location.get(id).get();
                if (thing.canReceive()) {
                    thing.handleReceive(character.removeItem(id));
                }
            } else {
                character.location.things.add(new Items(character.removeItem(id), 1));
            }
        }
    }

    public void handleTalk(PlayerCharacter character, String talkInfo) {
        if (character.isTalking()) {
            character.conversation.update(character, talkInfo);
        } else {
            character.location.things.stream()
                    .filter(thing -> (thing instanceof Talks) && thing.id().equals(talkInfo))
                    .findFirst().ifPresent(thing -> character.startConversation((Talks) thing));
        }
    }
}