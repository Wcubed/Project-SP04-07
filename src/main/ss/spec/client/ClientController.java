package ss.spec.client;

import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.Connection;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public void userInputNumber(int number) throws InvalidNumberException {
        if (canRequestGame()) {
            requestGame(number);
        } else {
            switch (model.getState()) {
                case MAKE_MOVE_DECIDE_TILE:
                    model.decideTile(number);
                    break;
                case MAKE_MOVE_DECIDE_BOARD_SPACE:
                    model.decideBoardSpace(number);
                    break;
                case MAKE_MOVE_DECIDE_ORIENTATION:
                    model.decideOrientation(number);

                    Move move = new Move(model.getSelectedTile(), model.getSelectedBoardSpace());

                    if (!model.getBoard().isMoveValid(move)) {
                        // Whoops, that is not valid.
                        model.invalidMoveAttempted();
                    }
                    // If we reached this without exceptions, we can send the move.

                    peer.sendMoveMessage(move);
                    break;
                case DECIDE_SKIP_OR_REPLACE:
                    model.decideSkipOrReplace(number);

                    if (model.getSelectedTile() == null) {
                        peer.sendSkipMessage();
                    } else {
                        peer.sendExchangeMessage(model.getSelectedTile());
                    }
                default:
                    // We were not expecting a number here.
                    throw new InvalidNumberException();
            }
        }
    }

    private boolean canRequestGame() {
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

    public void processMove(String name, Move move, int points) {
        // Don't check if the move is valid.
        // If the server says the move is made, then the move is made.
        model.processMove(name, move, points);
    }

    public void setTurn(String playerName) throws NoSuchPlayerException {
        model.setTurn(playerName);
    }


    public void updateWaitingForGame(List<String> names) {
        view.promptWaitingForGame(names);
    }

    public void invalidMoveAttempted() {
        model.invalidMoveAttempted();
    }


    private void requestGame(int numPlayers) throws InvalidNumberException {
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

    public void receiveChatMessage(String playerName, String message) {
        view.addChatMessage(playerName, message);
    }

    public void replaceTile(String playerName, Tile replacedTile, Tile replacingTile) {
        model.replaceTile(playerName, replacedTile, replacingTile);
    }

    public void playerSkipped(String playerName) {
        model.playerSkipped(playerName);
    }

    public void setTurnSkip(String playerName) throws NoSuchPlayerException {
        model.setTurnSkip(playerName);
    }

    public void leaderboardReturnToLobby(Map<String, Integer> leaderboard) {
        // Game is over.
        model = null;

        view.promptLeaderboardGameRequest(leaderboard);
    }

    public void playerLeftReturnToLobby(String playerName) {
        // Other player left, game is over!
        model = null;

        // Let the player know that we are back in the lobby.
        view.promptGameRequest();
    }
}
