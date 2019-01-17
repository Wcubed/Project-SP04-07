package ss.spec.client;

import ss.spec.networking.DeadConnectionException;
import ss.spec.networking.ServerPeer;
import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private static final int PORT = 4000;
    private ServerPeer server;

    public Client() {

    }

    public void start() {
        // TODO: Implement client logic. This is just to test the server.
        boolean running = true;

        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), PORT);
            SocketConnection connection = new SocketConnection(socket);
            server = new ServerPeer(connection);

            Thread connectionThread = new Thread(server);
            connectionThread.start();

            try {
                // TODO: Ask the TUI for a name, if we have a TUI.
                server.sendConnectMessage("Bob");
            } catch (DeadConnectionException e) {
                e.printStackTrace();
            }

            while (running) {
                try {
                    server.sendMessage("Hello world!");
                } catch (DeadConnectionException e) {
                    // Connection is gone, close program.
                    running = false;
                    // TODO: Nice logging.
                    System.out.println("Connection dead, closing program.");
                }

                // TODO: Remove sleeping for final application.
                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.start();
    }
}
