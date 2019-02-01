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

        peer = new ServerPeer(connection);

        this.view = new TuiView(
                this,
                new InputStreamReader(System.in),
                new OutputStreamWriter(System.out));

        // There is no game yet.
        this.model = null;
    }

    public void startViewThread() {
        Thread viewThread = new Thread(view);
        viewThread.start();
    }


    public void runPeer() {
        peer.run();
    }

    public void exitProgram() {
        view.closeView();
        peer.disconnect();
    }
}
