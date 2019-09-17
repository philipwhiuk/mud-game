package com.whiuk.philip.mud.server;

import com.whiuk.philip.mud.MudConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MudServer {

    private static AtomicLong nextThreadId = new AtomicLong();

    public static void main(String[] args) {
        try {
            MudServer mudServer = new MudServer(args.length > 0 ? args[0] : "");
            mudServer.run();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final String outputPrefix;
    private Set<String> accountCommands = new HashSet<>(Collections.singletonList(
            "LOGOUT"));
    private Set<String> clientCommands = new HashSet<>(Collections.singletonList(
            "QUIT"
    ));

    public void clientLoggedIn(Account account, Client client) {
        accountManager.setLoggedInToClient(account, client);
    }

    enum SocketState {
        WAITING_FOR_VERSION, CONNECTED
    }

    class SocketThread extends Thread {
        private final Socket socket;
        private final long id;
        private final BufferedReader inputStream;
        private final BufferedWriter outputStream;
        private SocketState state = SocketState.WAITING_FOR_VERSION;
        private Client client;

        SocketThread(long id, Socket socket) throws IOException {
            super("SocketThread" + id);
            this.id = id;
            this.socket = socket;
            this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        public void run() {
            String line;
            try {
                while ((line = inputStream.readLine()) != null) {
                    processMessage(line);
                }
            } catch (IOException e) {
                disconnectSocket(id, socket);
            }
        }

        private void processMessage(String message) {
            switch (state) {
                case WAITING_FOR_VERSION:
                    if (message.matches("v.*")) {
                        state = SocketState.CONNECTED;
                        client = createAndRegisterClient(this, message);
                    } else {
                        disconnectSocket(id, socket);
                    }
                    break;
                case CONNECTED:
                    client.processMessage(message);
            }
        }

        void sendMessage(String message) {
            try {
                outputStream.write(message);
                outputStream.newLine();
                outputStream.flush();
            } catch (IOException e) {
                disconnectSocket(id, socket);
            }
        }

        void disconnect() {
            disconnectSocket(id, socket);
        }
    }

    private AccountManager accountManager = new AccountManager();
    private Map<Long, SocketThread> socketThreads = new HashMap<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Client> clients = new ArrayList<>();
    private ThingStore thingStore;
    private RecipeStore recipeStore;
    private World world;
    private Chunk startingChunk;
    private Location startingLocation;
    private boolean running = true;

    private MudServer(String outputPrefix) throws IOException {
        this.outputPrefix = outputPrefix;
        ExperienceTable.initializeExpTable();
        thingStore = new ThingStore();
        recipeStore = new RecipeStore(thingStore);
        loadWorld();
        accountManager.init(world, startingChunk, startingLocation);
    }

    private void loadWorld() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("world.dat"))) {
            String[] worldSize = reader.readLine().split(",");
            world = new World(Integer.parseInt(worldSize[0]), Integer.parseInt(worldSize[1]));

            int chunkCount = Integer.parseInt(reader.readLine());
            for (int i = 0; i < chunkCount; i++) {
                loadChunk(reader);
            }

            String[] startingChunkCoOrds = reader.readLine().split(","); //5,5
            int startingChunkX = Integer.parseInt(startingChunkCoOrds[0]);
            int startingChunkY = Integer.parseInt(startingChunkCoOrds[1]);
            startingChunk = world.map[startingChunkX][startingChunkY];


            String[] startingLocationCoOrds = reader.readLine().split(","); //1,1,0
            int startingLocationX = Integer.parseInt(startingLocationCoOrds[0]);
            int startingLocationY = Integer.parseInt(startingLocationCoOrds[1]);
            int startingLocationZ = Integer.parseInt(startingLocationCoOrds[2]);
            startingLocation = startingChunk.map[startingLocationX][startingLocationY][startingLocationZ];
        }
    }

    private void loadChunk(BufferedReader reader) throws IOException {
        Biome biome = Biome.valueOf(reader.readLine());
        Location[][][] chunkMap = new Location[3][3][3];

        for (int z = 0; z < 3; z++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    chunkMap[x][y][z] = new Location(x, y, z, LocationType.valueOf(reader.readLine()));
                }
            }
        }
        Chunk chunk = new Chunk(thingStore, recipeStore, world, biome, chunkMap);
        int thingCount = Integer.parseInt(reader.readLine());
        for (int i = 0; i < thingCount; i++) {
            String id = reader.readLine();
            //TODO old thing not new thing
            Thing thing = thingStore.get(id).create();
            String[] thingCoOrds = reader.readLine().split(",");
            int thingX = Integer.parseInt(thingCoOrds[0]);
            int thingY = Integer.parseInt(thingCoOrds[1]);
            int thingZ = Integer.parseInt(thingCoOrds[2]);

            chunkMap[thingX][thingY][thingZ].add(thing);
        }


        String[] chunkCoOrds = reader.readLine().split(",");
        int chunkX = Integer.parseInt(chunkCoOrds[0]);
        int chunkY = Integer.parseInt(chunkCoOrds[1]);
        world.map[chunkX][chunkY] = chunk;
    }

    private void run() throws IOException {
        running = true;
        ServerSocket serverSocket = new ServerSocket(MudConstants.PORT);
        System.out.println(outputPrefix + "Now listening for connections on " + serverSocket.getLocalSocketAddress());

        while (running) {
            Socket newSocket = serverSocket.accept();
            long threadId = nextThreadId.incrementAndGet();
            SocketThread socketListener = new SocketThread(threadId, newSocket);
            socketListener.start();
            socketThreads.put(threadId, socketListener);
            System.out.println(outputPrefix +
                    "Socket connected - " + socketThreads.size() + " sockets connected.");
        }

        socketThreads.values().forEach(sT -> sT.sendMessage("BROADCAST - Server shutting down"));
        socketThreads.values().forEach(SocketThread::disconnect);
    }

    @SuppressWarnings("unused")
    void shutdown() {
        running = false;
    }

    private Client createAndRegisterClient(SocketThread socketThread, String message) {
        Client client = new Client(this, socketThread, message);
        clients.add(client);
        return client;
    }

    void handleMessageDuringLogin(Client client, String message) {
        String[] messageData = message.split(" ");
        String command = messageData[0];
        switch (command) {
            case "LOGIN":
                if (messageData.length != 3) {
                    badLoginMessage(client);
                } else {
                    handleLogin(client, messageData[1], messageData[2]);
                }
                break;
            case "HELP":
            case "?":
                client.sendMessage("Available commands: LOGIN, HELP, ?");
                break;
        }
    }

    private void badLoginMessage(Client client) {
        client.sendMessage("Login message format is: LOGIN <username> <password>");
    }

    private void handleLogin(Client client, String username, String password) {
        Account account = accountManager.getAccount(username);
        if (account != null) {
            account.login(client, password);
        } else {
            client.sendMessage("Incorrect username and/or password");
        }
    }

    void handleMessageDuringGame(Client client, Account account, String message) {
        String[] messageData = message.split(" ", 2);
        if (Game.CommandProcessor.gameCommands.contains(messageData[0])) {
            Game.CommandProcessor.handleMessage(account.character, message);
        } else if (accountCommands.contains(messageData[0])) {
            accountManager.handleMessage(account, message);
        } else if (clientCommands.contains(messageData[0])) {
            client.handleMessage(message);
        } else {
            client.sendMessage("Unknown command - type HELP for available commands");
        }
    }

    private void disconnectSocket(long id, Socket socket) {
        System.out.println(outputPrefix + "Disconnecting socket");
        socketThreads.remove(id);
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
