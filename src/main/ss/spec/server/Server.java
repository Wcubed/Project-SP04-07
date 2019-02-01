package ss.spec.server;

import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    // TODO: Some classes in the server need JML. See module guide for which ones and how.

    private static final int PORT = 4000;

    private ServerSocket serverSocket;
    private Lobby lobby;

    public Server() {
        lobby = new Lobby();
    }

    public void start() {
        // TODO: it is apparently a requirement that we be able to manually enter a port.
        //       but the protocoll states that we always use port 4000. So how to fix that?

        // TODO: Apparently all communication messages have to be written to std::out.
        //       Again, that's in the requirements. Can we do that in a nice way?

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
