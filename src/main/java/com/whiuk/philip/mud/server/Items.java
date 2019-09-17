package com.whiuk.philip.mud.server;

class Items extends Thing {
    final Item item;
    int count;

    Items(Item item, int count) {
        super(item.itemType);
        this.item = item;
        this.count = count;
    }

    @Override
    public String getDescription() {
        if (count > 1) {
            return item.getName() + " ("+count+")";
        } else {
            return item.getName();
        }
    }

    @Override
    public void tick(Chunk chunk, Location location) {

    }

    @Override
    boolean canTake() {
        return true;
    }

    @Override
    public Item handleTake(Location location) {
        if (!item.id().equals(this.item.id())) {
            throw new IllegalArgumentException();
        }
        if (count > 1) {
            count -= 1;
        } else {
            location.remove(this);
        }
        return item;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public void handleReceive(Item item) {
        if (!item.id().equals(this.item.id())) {
            throw new IllegalArgumentException();
        }
        count += 1;
    }
}