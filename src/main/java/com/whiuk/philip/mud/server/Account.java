package com.whiuk.philip.mud.server;

class Account {
    @SuppressWarnings("unused")
    final String username;
    private final String password;
    PlayerCharacter character;
    Client currentClient;

    Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    void setCharacter(PlayerCharacter character) {
        this.character = character;
    }

    public void sendMessage(String message) {
        currentClient.sendMessage(message);
    }

    public void logout() {
        currentClient.loggedOut();
        currentClient = null;
    }

    void login(Client client, String password) {
        if (this.password.equals(password)) {
            if (currentClient != null) {
                currentClient.handleLoggedInElsewhere();
            }
            client.loggedIn(this);
        } else {
            client.sendMessage("Incorrect username and/or password");
        }
    }
}