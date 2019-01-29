package ss.spec.client;

import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientController {

    private static final int PORT = 4000;

    String name;

    SpecView view;
    GameModel model;

    ServerPeer peer;

    public ClientController(String name, InetAddress serverAddress) throws IOException {
        this.name = name;

        Socket socket = new Socket(serverAddress, PORT);
        SocketConnection connection = new SocketConnection(socket);

        ServerPeer server;
        server = new ServerPeer(connection);

        this.view = new TuiView(
                new InputStreamReader(System.in),
                new OutputStreamWriter(System.out));

        // There is no game yet.
        this.model = null;
    }
}
