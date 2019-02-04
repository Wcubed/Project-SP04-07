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
        WAITING_FOR_MOVE_VALIDITY,
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

    // Variable to remember which tile the player selected.
    // To either make a move with, or to replace.
    private Tile selectedTile;

    // Variable to remember which board space the player selected to make a move on.
    private int selectedBoardSpace;

    public GameModel(List<Player> players, Player localPlayer, List<String> turnOrder) {
        // Todo: Decide whether the `players` list includes the localPlayer or not.
        this.players = new HashMap<>();

        for (Player player : players) {
            this.players.put(player.getName(), player);
        }

        this.localPlayer = localPlayer;
        this.turnOrder = new ArrayList<>(turnOrder);

        this.board = new Board();

        selectedTile = null;
        selectedBoardSpace = -1;

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

    /**
     * Only valid in certain states.
     *
     * @return The tile selected by the player.
     */
    public Tile getSelectedTile() {
        return selectedTile;
    }

    /**
     * Only valid in certain states.
     *
     * @return The board space id selected by the player.
     */
    public int getSelectedBoardSpace() {
        return selectedBoardSpace;
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

            // Clear out the selections.
            selectedTile = null;
            selectedBoardSpace = -1;

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

    /**
     * Attempt to decide a tile to use from the hand.
     * If successful, the model will continue to the MAKE_MOVE_DECIDE_BOARD_SPACE state.
     *
     * @param tileNumber The tile to select from the hand.
     */
    public void decideTile(int tileNumber) throws InvalidNumberException {
        if (tileNumber >= 0 && tileNumber < localPlayer.getTiles().size()) {
            selectedTile = localPlayer.getTiles().get(tileNumber);
            currentState = State.MAKE_MOVE_DECIDE_BOARD_SPACE;

            setChanged();
            notifyObservers(Change.MOVE_DECISION_PROGRESS);
        } else {
            // Whoops, that tile does not exist.
            selectedTile = null;
            currentState = State.MAKE_MOVE_DECIDE_TILE;

            throw new InvalidNumberException();
        }
    }

    public void decideBoardSpace(int spaceId) throws InvalidNumberException {
        if (spaceId >= 0 && spaceId < Board.BOARD_SIZE) {
            selectedBoardSpace = spaceId;
            currentState = State.MAKE_MOVE_DECIDE_ORIENTATION;

            setChanged();
            notifyObservers(Change.MOVE_DECISION_PROGRESS);
        } else {
            // Whoops, that tile does not exist.
            selectedBoardSpace = -1;
            currentState = State.MAKE_MOVE_DECIDE_BOARD_SPACE;

            throw new InvalidNumberException();
        }
    }

    public void decideOrientation(int orientationNumber) throws InvalidNumberException {
        if (orientationNumber >= 0 && orientationNumber < 3) {
            switch (orientationNumber) {
                case 0:
                    // It's already at orientation 0.
                    break;
                case 1:
                    selectedTile = selectedTile.rotate120();
                    break;
                case 2:
                    selectedTile = selectedTile.rotate240();
            }

            currentState = State.WAITING_FOR_MOVE_VALIDITY;

            setChanged();
            notifyObservers(Change.MOVE_DECISION_PROGRESS);
        } else {
            // Whoops, only 0-2 are okay.
            currentState = State.MAKE_MOVE_DECIDE_ORIENTATION;
            throw new InvalidNumberException();
        }
    }
}
