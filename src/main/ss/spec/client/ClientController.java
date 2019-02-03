package ss.spec.client;

import ss.spec.gamepieces.Tile;
import ss.spec.networking.Connection;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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

    public void startGame(List<String> turnOrder) throws GameStartedWithoutUsException {

        ArrayList<Player> players = new ArrayList<>();
        Player localPlayer = null;

        for (String name : turnOrder) {
            Player player = new Player(name);
            players.add(player);

            if (name.equals(this.name)) {
                // This is us :)
                localPlayer = player;
            }
        }

        if (localPlayer == null) {
            // This game does not include us?!
            throw new GameStartedWithoutUsException();
        }

        model = new GameModel(players, localPlayer, turnOrder);
        model.addObserver(view);

        // We don't need to let the player know anything.
        // Because we will immediately get a "tiles and turn" message.
    }

    public void setPlayerHand(String playerName, List<Tile> hand) throws NoSuchPlayerException {
        model.setPlayerHand(playerName, hand);
    }

    public void setTurn(String playerName) throws NoSuchPlayerException {
        model.setTurn(playerName);
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
