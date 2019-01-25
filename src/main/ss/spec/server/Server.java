package ss.spec.server;

import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    private static final int PORT = 4000;

    private ServerSocket serverSocket;
    private Lobby lobby;

    public Server() {
        lobby = new Lobby();
    }

    public void start() {
        // try to open a server socket;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("ERROR: could not create a socket on port " + PORT);
            return;
        }

        // Start the lobby thread.
        Thread lobbyThread = new Thread(lobby);
        lobbyThread.start();

        System.out.println("Server up and running.");

        while (true) {
            Socket clientSocket;

            try {
                // Wait for clients to connect.
                clientSocket = serverSocket.accept();

                SocketConnection connection = new SocketConnection(clientSocket);
                ClientPeer newClient = new ClientPeer(connection);

                System.out.println("New client connected!");

                Thread newConnectionThread = new Thread(newClient);
                newConnectionThread.start();

                // Hand the client off to the lobby.
                lobby.addNewClient(newClient);

            } catch (IOException e) {
                // TODO: Nice error handling.
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server();

        server.start();
    }
}
