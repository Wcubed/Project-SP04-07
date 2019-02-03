package ss.spec.client;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Tile;

import java.util.*;

public class GameModel extends Observable {

    public enum State {
        WAITING_FOR_TURN,
        MAKE_MOVE,
        DECIDE_SKIP
    }

    public enum Change {
        TURN_ADVANCES,
    }

    private Board board;

    private Player localPlayer;

    private HashMap<String, Player> players;
    private ArrayList<String> turnOrder;
    private Player currentTurnPlayer;

    public GameModel(List<Player> players, Player localPlayer, List<String> turnOrder) {
        // Todo: Decide whether the `players` list includes the localPlayer or not.
        this.players = new HashMap<>();

        for (Player player : players) {
            this.players.put(player.getName(), player);
        }

        this.localPlayer = localPlayer;
        this.turnOrder = new ArrayList<>(turnOrder);

        this.board = new Board();
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

    // ---------------------------------------------------------------------------------------------

    public void setTurn(String playerName) throws NoSuchPlayerException {
        if (!players.containsKey(playerName)) {
            throw new NoSuchPlayerException();
        }

        currentTurnPlayer = players.get(playerName);

        setChanged();
        notifyObservers(Change.TURN_ADVANCES);

    }

    public void setPlayerHand(String playerName, List<Tile> hand) throws NoSuchPlayerException {
        if (players.containsKey(playerName)) {
            players.get(playerName).overrideTiles(hand);
        } else {
            throw new NoSuchPlayerException();
        }
    }
}
