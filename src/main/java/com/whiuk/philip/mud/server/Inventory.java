package com.whiuk.philip.mud.server;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class Inventory {

    private Map<String, Items> storage = new HashMap<>();

    public void receiveItem(Item item) {
        if (storage.containsKey(item.id())) {
            storage.get(item.id()).count += 1;
        } else {
            storage.put(item.id(), new Items(item, 1));
        }
    }
    public void receiveItems(Items items) {
        if (storage.containsKey(items.item.id())) {
            storage.get(items.item.id()).count += items.count;
        } else {
            storage.put(items.item.id(), items);
        }
    }

    public Item removeItem(String item) {
        if (storage.get(item) == null) {
            return null;
        }
        if (storage.get(item).count == 1) {
            return storage.remove(item).item;
        } else {
            storage.get(item).count -= 1;
            return storage.get(item).item;
        }
    }

    public boolean removeItems(Items items) {
        if (storage.get(items.item.id()) == null) {
            return false;
        }
        if (storage.get(items.item.id()).count < items.count){
            return false;
        }
        if (storage.get(items.item.id()).count == items.count) {
            storage.remove(items.item.id());
        } else {
            storage.get(items.item.id()).count -= items.count;
        }
        return true;
    }

    public boolean hasItem(String target) {
        return storage.containsKey(target);
    }

    Stream<String> toMessageStream() {
        if (storage.isEmpty()) {
            return Stream.of("No items");
        }
        return storage.entrySet().stream().map(e -> e.getKey() + " " + e.getValue().getDescription());
    }

    public Item getItem(String itemId) {
        return storage.get(itemId).item;
    }
}
