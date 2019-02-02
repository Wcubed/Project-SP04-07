package ss.spec.client;

import ss.spec.networking.Connection;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientController {
    private String name;
    private boolean serverSupportsChat;

    private SpecView view;
    private GameModel model;

    private ServerPeer peer;

    public ClientController(String name, Connection connection, boolean serverSupportsChat) {
        this.name = name;
        this.serverSupportsChat = serverSupportsChat;

        peer = new ServerPeer(this, connection, serverSupportsChat);

        this.view = new TuiView(
                this,
                new InputStreamReader(System.in),
                new OutputStreamWriter(System.out),
                serverSupportsChat);

        // There is no game yet.
        this.model = null;

        // Start by asking the player with how many players they want to play.
        this.view.promptGameRequest();
    }

    public boolean canRequestGame() {
        // If we have no game model, we can go and request a game.
        return this.model == null;
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

    // ---------------------------------------------------------------------------------------------

    public void requestGame(int numPlayers) throws InvalidNumberException {
        if (numPlayers < 2 || numPlayers > 4) {
            throw new InvalidNumberException();
        }

        peer.sendRequestMessage(numPlayers);
    }

    public void sendChatMessage(String message) {
        if (serverSupportsChat) {
            peer.sendChatMessage(message);
        }
    }

    public void receiveChatMessage(String name, String message) {
        view.addChatMessage(name, message);
    }
}
