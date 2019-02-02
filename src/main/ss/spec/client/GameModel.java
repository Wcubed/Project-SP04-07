package ss.spec.client;

import ss.spec.gamepieces.Board;

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

    public void setTurn(String playerName) {
        Player currentPlayer = players.get(playerName);

        if (currentPlayer == null) {
            // This should not happen.
            // TODO: maybe return an exception?
            return;
        }

        currentTurnPlayer = currentPlayer;

        setChanged();
        notifyObservers(Change.TURN_ADVANCES);

    }
}
