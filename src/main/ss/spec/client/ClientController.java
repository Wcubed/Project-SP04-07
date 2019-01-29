package ss.spec.client;

import ss.spec.networking.Connection;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientController {
    private String name;

    private SpecView view;
    private GameModel model;

    private ServerPeer peer;

    public ClientController(String name, Connection connection) {
        this.name = name;

        ServerPeer peer;
        peer = new ServerPeer(connection);

        this.view = new TuiView(
                new InputStreamReader(System.in),
                new OutputStreamWriter(System.out));

        // There is no game yet.
        this.model = null;
    }
}
