package ss.spec.client;

import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private static final int PORT = 4000;

    private static final String USAGE = "usage: <name> <address>";

    String name;
    InetAddress address;

    public Client(String name, InetAddress address) {
        this.name = name;
        this.address = address;
    }

    public void start() {

        try {
            Socket socket = new Socket(address, PORT);
            SocketConnection connection = new SocketConnection(socket);

            ServerPeer server;
            server = new ServerPeer(connection);

            Thread connectionThread = new Thread(server);
            connectionThread.start();


            server.sendConnectMessage(name);

            while (server.isPeerConnected()) {
                server.sendMessage("Hello world!");

                // TODO: Remove sleeping for final application.
                Thread.sleep(1000);
            }

            System.out.println("Server disconnected, exiting.");

        } catch (ConnectException e) {
            // TODO: nice error handling.
            System.out.println("Something went wrong while connecting:");
            e.printStackTrace();
        } catch (IOException | InterruptedException e) {
            // TODO: nice error handling.
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(USAGE);
            System.exit(0);
        }

        String name = args[0];
        InetAddress addr = null;

        // check args[1] - the IP-address.
        try {
            addr = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            System.out.println(USAGE);
            System.out.println("ERROR: host " + args[1] + " unknown");
            System.exit(0);
        }


        Client client = new Client(name, addr);

        client.start();
    }
}
