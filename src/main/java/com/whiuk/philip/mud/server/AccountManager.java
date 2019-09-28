package com.whiuk.philip.mud.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AccountManager {
    private Map<String, Account> accounts = new HashMap<>();
    private World world;
    private ThingStore thingStore;

    public void init(ThingStore thingStore, World world, Chunk startingChunk, Location startingLocation) {
        this.world = world;
        loadAccounts(thingStore, startingChunk, startingLocation);
    }

    private void loadAccounts(ThingStore thingStore,
                              Chunk startingChunk, Location startingLocation) {
        Account a = new Account("a", "b");
        PlayerCharacter p = new PlayerCharacter(
                a, "Thurgo", startingChunk, startingLocation, new Inventory(), Collections.emptyMap(), Collections.emptyMap());
        p.receiveItem((Equipment) thingStore.get("AXE").create());
        a.setCharacter(p);

        accounts.put(a.username, a);
    }

    public void setLoggedInToClient(Account account, Client client) {
        account.currentClient = client;
        world.playerJoined(account.character);
    }

    public Account getAccount(String username) {
        return accounts.get(username);
    }

    void handleMessage(Account account, String message) {
        String[] messageData = message.split(" ");
        switch (messageData[0]) {
            case "LOGOUT":
                logoutAccount(account);
        }
    }

    private void logoutAccount(Account account) {
        world.playerLeaving(account.character);
        account.logout();
    }
}
