package ss.spec.client;

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

        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), PORT);
            SocketConnection connection = new SocketConnection(socket);
            server = new ServerPeer(connection);

            Thread connectionThread = new Thread(server);
            connectionThread.start();

            // TODO: Ask the TUI for a name, if we have a TUI.
            server.sendConnectMessage("Bob");

            while (server.isPeerConnected()) {
                server.sendMessage("Hello world!");

                // TODO: Remove sleeping for final application.
                Thread.sleep(1000);
            }

            System.out.println("Server disconnected, exiting.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();

        client.start();
    }
}
