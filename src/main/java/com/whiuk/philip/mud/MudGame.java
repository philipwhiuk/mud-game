package com.whiuk.philip.mud;

import com.whiuk.philip.mud.client.MudClient;
import com.whiuk.philip.mud.server.MudServer;

public class MudGame {
    public static void main(String[] args) {
        new Thread(() -> {
            MudClient.main(new String[]{"Client: "});
        }).start();
        new Thread(() -> {
            MudServer.main(new String[]{"Server: "});
        }).start();
    }

}
