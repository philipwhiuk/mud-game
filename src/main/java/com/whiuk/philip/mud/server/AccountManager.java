package com.whiuk.philip.mud.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AccountManager {
    private Map<String, Account> accounts = new HashMap<>();
    private World world;

    public void init(World world, Chunk startingChunk, Location startingLocation) {
        this.world = world;
        loadAccounts(startingChunk, startingLocation);
    }

    private void loadAccounts(Chunk startingChunk, Location startingLocation) {
        Account a = new Account("a", "b");
        PlayerCharacter p = new PlayerCharacter(
                a, "Thurgo", startingChunk, startingLocation, new Inventory(), Collections.emptyMap(), Collections.emptyMap());
        EquipmentType ironAxe = new EquipmentType("AXE", "Iron axe", Slot.MAIN_HAND);
        p.receiveItem(new Item(ironAxe));
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
