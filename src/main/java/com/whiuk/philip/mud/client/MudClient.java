package com.whiuk.philip.mud.client;

import com.whiuk.philip.mud.MudConstants;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

import static com.whiuk.philip.mud.Messages.*;

public class MudClient {
    private static final String VERSION = "v0.0.1";

    public static void main(String[] args) {
        try {
            new MudClient(args.length > 0 ? args[0] : "");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    class ServerDetails {
        private final String hostname;
        private final int port;

        ServerDetails(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }
    }

    class ClientSocketHandler extends Thread {

        private final InputStream input;
        private final OutputStream output;
        private final Socket socket;

        ClientSocketHandler(Socket socket) throws IOException {
            this.socket = socket;
            input = socket.getInputStream();
            output = socket.getOutputStream();
        }

        @Override
        public void run() {
            socketConnected();
            try {
                while (connected) {
                    Message message = Message.parseDelimitedFrom(input);
                    receivedMessage(message.getText());
                }
            } catch (IOException e) {
                handleNetworkError();
            }
        }

        void sendMessage(String s) {
            try {
                Message.newBuilder()
                        .setText(s).build().writeDelimitedTo(output);
                output.flush();
            } catch (IOException e) {
                handleNetworkError();
            }
        }

        private void handleNetworkError() {
            printMessage("Unable to send message to server - disconnected");
            disconnectSocket();
        }

        private void disconnectSocket() {
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException ignored) {
            }
            connected = false;
        }
    }


    private volatile boolean running = true;
    private volatile boolean connected = true;
    private volatile ClientSocketHandler socketHandler;
    private final BufferedReader consoleReader;
    private final String outputPrefix;

    private MudClient(String outputPrefix) throws IOException {
        this.outputPrefix = outputPrefix;
        consoleReader = new BufferedReader(new InputStreamReader(System.in));
        printMessage("MUD Client " + VERSION);

        while (running) {
            Optional<ServerDetails> optionalServerDetails = askForConnectionDetails();
            if (optionalServerDetails.isPresent()) {
                ServerDetails serverDetails = optionalServerDetails.get();
                try (Socket socket = new Socket(serverDetails.hostname, serverDetails.port)) {
                    socketHandler = new ClientSocketHandler(socket);
                    socketHandler.start();

                    String line;
                    while (running && connected && (line = readLine()) != null) {
                        processLine(line);
                    }
                } catch (ConnectException e) {
                    printMessage("Failed to connect to server: Connection failed");
                }
                socketHandler = null;
            }
        }
    }

    private Optional<ServerDetails> askForConnectionDetails() {
        printMessage("To connect to the default server type 'CONNECT'");
        printMessage("To connect to a different server type 'CONNECT <hostname> <port>'");
        return waitForConnectionDetails();
    }

    private Optional<ServerDetails> waitForConnectionDetails() {
        String line;
        while ((line = readLine()) != null) {
            String[] messageData = line.split(" ");
            if (messageData.length == 1 && messageData[0].equals("QUIT")) {
                running = false;
                return Optional.empty();
            }
            if (messageData.length != 1 && messageData.length != 3 || !messageData[0].equals("CONNECT")) {
                printMessage("Incorrect CONNECT format");
            } else if (messageData.length == 1) {
                return Optional.of(new ServerDetails(MudConstants.HOSTNAME, MudConstants.PORT));
            } else {
                try {
                    int port = Integer.parseInt(messageData[2]);
                    return Optional.of(new ServerDetails(messageData[1], port));
                } catch (NumberFormatException e) {
                    printMessage("Incorrect port format for CONNECT");
                }
            }
        }
        return Optional.empty();
    }

    private String readLine() {
        try {
            return consoleReader.readLine();
        } catch (IOException e) {
            running = false;
            System.exit(1);
            return "";
        }
    }

    private void printMessage(String message) {
        System.out.println(outputPrefix+message);
    }

    private void processLine(String line) {
        if (line.equals("QUIT")) {
            socketHandler.sendMessage(line);
            socketHandler.disconnectSocket();
            running = false;
            System.exit(0);
        } else {
            socketHandler.sendMessage(line);
        }
    }

    private void socketConnected() {
        socketHandler.sendMessage(VERSION);
    }

    private void receivedMessage(String message) {
        System.out.println(message);
    }
}
