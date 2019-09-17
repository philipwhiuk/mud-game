package com.whiuk.philip.mud.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class PlayerCharacter {

    public boolean isTalking() {
        return conversation != null;
    }

    public void startConversation(Talks thingThatTalks) {
        conversation = new Conversation(thingThatTalks);
        conversation.start(this);
    }

    static class CommandProcessor {

        public static void handleEquipment(PlayerCharacter p) {
            if (p.equipment.isEmpty()) {
                p.sendMessage("No equipment");
            } else {
                p.equipment.forEach((key, value) -> p.sendMessage(key + " - " + value.getName()));
            }
        }

        public static void handleInventory(PlayerCharacter p) {
            p.inventory.toMessageStream().forEach(p::sendMessage);
        }

        public static void handleSkills(PlayerCharacter p) {
            if (p.equipment.isEmpty()) {
                p.sendMessage("No experience");
            } else {
                p.skills.forEach((key, value) -> p.sendMessage(key + " - " + value.level()));
            }
        }

    }

    private final Account account;
    public boolean firstLogin;
    String name;

    Chunk chunk;
    Location location;
    Stance stance;
    private Inventory inventory;
    private int nextOfferId = 0;
    private Map<Integer, Offer> offers;
    private HashMap<Slot, Equipment> equipment;
    HashMap<String, Experience> skills;
    private Task runningTask;
    public Conversation conversation;

    PlayerCharacter(Account account,
                    String name,
                    Chunk chunk, Location location,
                    Inventory inventory, Map<Slot, Equipment> equipment,
                    Map<String, Experience> skills) {
        this.account = account;
        this.name = name;
        this.chunk = chunk;
        this.location = location;
        this.inventory = inventory;
        this.equipment = new HashMap<>(equipment);
        this.skills = new HashMap<>(skills);
        stance = Stance.STANDING;
    }

    public void sendMessage(String message) {
        account.sendMessage(message);
    }


    public void createOfferFromPlayer(String give, String take, PlayerCharacter originator) {
        Offer offer = new Offer(nextOfferId++, give, take, originator);
        offers.put(offer.id, offer);
        sendMessage(
                "Offer " + offer.id + " from " + originator.name + " of " + give + " for " + take + ". ACCEPT / REJECT");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canMove() {
        //TODO: Can't move in combat.
        return true;
    }

    public void receiveItem(Item item) {
        inventory.receiveItem(item);
    }

    public Item removeItem(String item) {
        return inventory.removeItem(item);
    }

    public void handleEquip(String itemId) {
        if (inventory.hasItem(itemId)) {
            Item item = inventory.removeItem(itemId);
            if (item instanceof Equipment) {
                Equipment e = (Equipment) item;
                if (equipment.containsKey(e.getSlot())) {
                    unequipItem(e.getSlot());
                }
                equipment.put(e.getSlot(), e);
            } else {
                inventory.receiveItem(item);
            }
        }
    }

    public void handleEat(PlayerCharacter character, String itemId) {
        if (inventory.hasItem(itemId)) {
            Item item = inventory.removeItem(itemId);
            if (item instanceof Edible) {
                ((Edible) item).eat(character);
            } else {
                inventory.receiveItem(item);
            }
        }
    }

    public void handleDrink(PlayerCharacter character, String itemId) {
        if (inventory.hasItem(itemId)) {
            Item item = inventory.removeItem(itemId);
            if (item instanceof Drinkable) {
                ((Drinkable) item).drink(character);
            } else {
                inventory.receiveItem(item);
            }
        }
    }

    public void handleUnequip(String slot) {
        if (equipment.containsKey(Slot.valueOf(slot))) {
            unequipItem(Slot.valueOf(slot));
        }
    }

    public boolean hasItem(String id) {
        return inventory.hasItem(id) || equipment.entrySet().stream().anyMatch(e -> e.getValue().id().equals(id));
    }

    private void unequipItem(Slot slot) {
        inventory.receiveItem(equipment.remove(slot));
    }

    public void handleOfferAccept(String offerId) {
        Offer offer = offers.get(Integer.parseInt(offerId));
        if (offer == null) {
            sendMessage("Offer doesn't exist");
        } else {
            chunk.handleOfferAccept(this, offer);
        }
    }

    public void handleOfferReject(String offerId) {
        Offer offer = offers.get(Integer.parseInt(offerId));
        if (offer == null) {
            sendMessage("Offer doesn't exist");
        } else {
            chunk.handleOfferReject(this, offer);
        }
    }

    public Item getItem(String itemId) {
        return inventory.getItem(itemId);
    }

    public boolean hasItem(String id, int quantity) {
        //TODO:
        return false;
    }

    public boolean hasSpaceForItems(int totalCount) {
        return true;
    }

    public void startTask(ScheduledExecutorService taskQueue, Runnable task, long taskLength, String cancelledMessage) {
        if (this.runningTask != null) {
            this.runningTask.future.cancel(false);
            if (this.runningTask.future.isCancelled()) {
                sendMessage(this.runningTask.cancelledMessage);
            }
        }
        this.runningTask = new Task(taskQueue.schedule(task, taskLength, TimeUnit.MILLISECONDS), cancelledMessage);
    }
}