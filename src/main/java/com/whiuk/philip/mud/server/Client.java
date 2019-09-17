package com.whiuk.philip.mud.server;

class Client {
    private static final String WELCOME_MESSAGE =
            "Welcome to the MUD adventurer. To login type 'LOGIN <username> <password>. For other commands type ? or HELP";
    private final MudServer server;
    private final MudServer.SocketThread socketThread;
    @SuppressWarnings("unused")
    private final String version;
    private ClientState state = ClientState.LOGIN;
    private Account account;

    Client(MudServer server, MudServer.SocketThread socketThread, String version) {
        this.server = server;
        this.socketThread = socketThread;
        this.version = version;
        socketThread.sendMessage(WELCOME_MESSAGE);
    }

    public void processMessage(String message) {
        switch (state) {
            case LOGIN:
                server.handleMessageDuringLogin(this, message);
                break;
            case GAME:
                server.handleMessageDuringGame(this, account, message);
        }
    }

    void sendMessage(String s) {
        socketThread.sendMessage(s);
    }

    void loggedIn(Account account) {
        this.account = account;
        state = ClientState.GAME;
        sendMessage("Login successful..");
        server.clientLoggedIn(account, this);

    }

    void handleLoggedInElsewhere() {
        sendMessage("Your account has been logged in elsewhere!");
        account.logout();
    }

    void loggedOut() {
        account = null;
        state = ClientState.LOGIN;
        sendMessage("You are now logged out");
    }

    void handleMessage(String message) {
        String[] messageData = message.split(" ");
        switch (messageData[0]) {
            case "QUIT":
                if (account != null) {
                    account.logout();
                    account = null;
                }
                socketThread.disconnect();
        }
    }
}