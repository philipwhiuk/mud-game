package com.whiuk.philip.mud.server;

import com.whiuk.philip.mud.Messages;
import com.whiuk.philip.mud.MudConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        } catch (IOException | ParseException e) {
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
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private SocketState state = SocketState.WAITING_FOR_VERSION;
        private Client client;

        SocketThread(long id, Socket socket) throws IOException {
            super("SocketThread" + id);
            this.id = id;
            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
        }

        public void run() {
            try {
                while (running) {
                    Messages.Message message = Messages.Message.parseDelimitedFrom(inputStream);
                    processMessage(message.getText());
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
                Messages.Message.newBuilder().setText(message).build().writeDelimitedTo(outputStream);
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

    private MudServer(String outputPrefix) throws IOException, ParseException {
        this.outputPrefix = outputPrefix;
        ExperienceTable.initializeExpTable();
        thingStore = new ThingStore();
        recipeStore = new RecipeStore(thingStore);
        loadWorld();
        accountManager.init(world, startingChunk, startingLocation);
    }

    private void loadWorld() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("world.json")) {
            JSONObject worldData = (JSONObject) jsonParser.parse(reader);
            int x = ((Long) worldData.get("x")).intValue();
            int y = ((Long) worldData.get("x")).intValue();

            world = new World(x,y);

            JSONArray chunks = (JSONArray) worldData.get("chunks");

            for (Object chunk : chunks) {
                loadChunk((JSONObject) chunk);
            }

            JSONObject startingChunk = (JSONObject) worldData.get("startingChunk");

            int startingChunkX = ((Long) startingChunk.get("x")).intValue();
            int startingChunkY = ((Long) startingChunk.get("y")).intValue();
            this.startingChunk = world.map[startingChunkX][startingChunkY];


            JSONObject startingLocation = (JSONObject) worldData.get("startingLocation");

            int startingLocationX = ((Long) startingLocation.get("x")).intValue();
            int startingLocationY = ((Long) startingLocation.get("y")).intValue();
            int startingLocationZ = ((Long) startingLocation.get("z")).intValue();
            this.startingLocation = this.startingChunk.map[startingLocationX][startingLocationY][startingLocationZ];
        }
    }

    private void loadChunk(JSONObject chunkData) {
        Biome biome = Biome.valueOf((String) chunkData.get("biome"));
        Location[][][] chunkMap = new Location[3][3][3];

        JSONArray locations = (JSONArray) chunkData.get("locations");
        Iterator locationIterator = locations.iterator();

        for (int z = 0; z < 3; z++) {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    String locationType = (String) locationIterator.next();
                    chunkMap[x][y][z] = new Location(x, y, z,
                            LocationType.valueOf(locationType));
                }
            }
        }
        Chunk chunk = new Chunk(thingStore, recipeStore, world, biome, chunkMap);
        JSONArray things = (JSONArray) chunkData.get("things");
        for (Object thingJS : things) {
            JSONObject thingData = (JSONObject) thingJS;
            Thing thing = thingStore.get((String) thingData.get("type")).create();
            int thingX = ((Long) thingData.get("x")).intValue();
            int thingY = ((Long) thingData.get("y")).intValue();
            int thingZ = ((Long) thingData.get("z")).intValue();

            chunkMap[thingX][thingY][thingZ].add(thing);
        }


        int chunkX = ((Long) chunkData.get("x")).intValue();
        int chunkY = ((Long) chunkData.get("y")).intValue();
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
