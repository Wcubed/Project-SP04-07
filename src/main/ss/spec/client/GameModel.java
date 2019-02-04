package ss.spec.client;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Tile;

import java.util.*;

public class GameModel extends Observable {

    public enum State {
        WAITING_FOR_TURN,
        MAKE_MOVE_DECIDE_TILE,
        MAKE_MOVE_DECIDE_BOARD_SPACE,
        MAKE_MOVE_DECIDE_ORIENTATION,
        DECIDE_SKIP,
        DECIDE_REPLACE_TILE,
    }

    public enum Change {
        TURN_ADVANCES,
        TURN_ADVANCES_OUR_TURN,
        MOVE_DECISION_PROGRESS,
    }

    private Board board;

    private Player localPlayer;

    private HashMap<String, Player> players;
    private ArrayList<String> turnOrder;
    private Player currentTurnPlayer;

    private State currentState;

    public GameModel(List<Player> players, Player localPlayer, List<String> turnOrder) {
        // Todo: Decide whether the `players` list includes the localPlayer or not.
        this.players = new HashMap<>();

        for (Player player : players) {
            this.players.put(player.getName(), player);
        }

        this.localPlayer = localPlayer;
        this.turnOrder = new ArrayList<>(turnOrder);

        this.board = new Board();

        // We start off waiting for our turn.
        currentState = State.WAITING_FOR_TURN;
    }

    // ---------------------------------------------------------------------------------------------

    public Board getBoard() {
        return board;
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public List<String> getTurnOrder() {
        return turnOrder;
    }

    public Player getCurrentTurnPlayer() {
        return currentTurnPlayer;
    }

    public State getState() {
        return currentState;
    }

    // ---------------------------------------------------------------------------------------------

    public void setTurn(String playerName) throws NoSuchPlayerException {
        if (!players.containsKey(playerName)) {
            throw new NoSuchPlayerException();
        }

        currentTurnPlayer = players.get(playerName);

        setChanged();

        if (currentTurnPlayer.equals(localPlayer)) {
            // Hey! It's our turn now.
            currentState = State.MAKE_MOVE_DECIDE_TILE;
            notifyObservers(Change.TURN_ADVANCES_OUR_TURN);
        } else {
            // Someone else's turn.
            currentState = State.WAITING_FOR_TURN;
            notifyObservers(Change.TURN_ADVANCES);
        }
    }

    public void setPlayerHand(String playerName, List<Tile> hand) throws NoSuchPlayerException {
        if (players.containsKey(playerName)) {
            players.get(playerName).overrideTiles(hand);
        } else {
            throw new NoSuchPlayerException();
        }
    }
}
