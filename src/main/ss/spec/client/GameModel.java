package ss.spec.client;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;

import java.util.*;

public class GameModel extends Observable {
	//@ invariant getPlayers().containsValue(getCurrentTurnPlayer());
	//@ invariant getPlayers().containsValue(getLocalPlayer());
	//@ invariant getPlayers().size() == getTurnOrder().size();
	//@ invariant (\forall Player player; getPlayers().containsValue(player); getTurnOrder().contains(player.getName()));
	//@ invariant (\forall String name; getPlayers().containsKey(name); getTurnOrder().contains(name));
	//@ invariant (\forall String name; getPlayers().containsKey(name); getPlayers().get(name).getName().equals(name));
	//@ invariant getBoard() != null;
	//@ invariant getLocalPlayer() != null;
	//@ invariant getTurnOrder() != null;
	//@ invariant getCurrentTurnPlayer() != null;
	//@ invariant getState() != null;

    public enum State {
        WAITING_FOR_TURN,
        MAKE_MOVE_DECIDE_TILE,
        MAKE_MOVE_DECIDE_BOARD_SPACE,
        MAKE_MOVE_DECIDE_ORIENTATION,
        WAITING_FOR_MOVE_VALIDITY,
        DECIDE_SKIP_OR_REPLACE,
        WAITING_FOR_SKIP_REPLACE_VALIDITY,
    }

    public enum Change {
        TURN_ADVANCES,
        TURN_ADVANCES_OUR_TURN,
        MOVE_DECISION_PROGRESS,
        INVALID_MOVE_ATTEMPTED,
        MOVE_MADE,
        TILE_REPLACED,
        PLAYER_SKIPPED,
    }

    private final Board board;

    private final Player localPlayer;

    private final HashMap<String, Player> players;
    private final ArrayList<String> turnOrder;
    private Player currentTurnPlayer;

    private State currentState;

    // Variable to remember which tile the player selected.
    // To either make a move with, or to replace.
    private Tile selectedTile;

    // Variable to remember which board space the player selected to make a move on.
    private int selectedBoardSpace;

    /**
     * Constructs a new GameModel, do this when the server signals the start of a new game.
     *
     * @param players     The participating players, includes the localPlayer.
     * @param localPlayer This is our local player, or `us`.
     * @param turnOrder   The order in which the players have their turns.
     */
    //@ requires players.contains(localPlayer);
    //@ requires (\forall Player player; players.contains(player); turnOrder.contains(player.getName()));
    //@ requires players.size() == turnOrder.size();
    //@ ensures getLocalPlayer().equals(localPlayer);
    //@ ensures getTurnOrder().equals(turnOrder);
    //@ ensures (\forall Player player; players.contains(player); getPlayers().containsValue(player));
    //@ ensures getTurnOrder().size() == getPlayers().size();
    //@ ensures getState().equals(State.WAITING_FOR_TURN);
    public GameModel(List<Player> players, Player localPlayer, List<String> turnOrder) {
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

    //@ pure
    public Board getBoard() {
        return board;
    }

    //@ pure
    public Player getLocalPlayer() {
        return localPlayer;
    }

    //@ ensures (\forall String name; \result.containsKey(name); \result.get(name).getName().equals(name));
    //@ pure
    public Map<String, Player> getPlayers() {
        return players;
    }

    //@ pure
    public List<String> getTurnOrder() {
        return turnOrder;
    }

    //@ ensures getPlayers().containsValue(\result);
    //@ pure
    public Player getCurrentTurnPlayer() {
        return currentTurnPlayer;
    }

    //@ pure
    public State getState() {
        return currentState;
    }

    /**
     * Only valid in certain states.
     *
     * @return The tile selected by the player.
     */
    /*@ requires getState() == State.MAKE_MOVE_DECIDE_BOARD_SPACE ||
                 getState() == State.MAKE_MOVE_DECIDE_ORIENTATION ||
                 getState() == State.WAITING_FOR_MOVE_VALIDITY ||
                 getState() == State.WAITING_FOR_SKIP_REPLACE_VALIDITY;
     */
    /*@ ensures (getState() == State.MAKE_MOVE_DECIDE_BOARD_SPACE ||
                getState() == State.MAKE_MOVE_DECIDE_ORIENTATION ||
                getState() == State.WAITING_FOR_MOVE_VALIDITY) &&
                \result != null;
      */
    //@ pure
    public Tile getSelectedTile() {
        return selectedTile;
    }

    /**
     * Only valid in certain states.
     *
     * @return The board space id selected by the player.
     */
    /*@ requires getState() == State.MAKE_MOVE_DECIDE_BOARD_SPACE ||
    		     getState() == State.MAKE_MOVE_DECIDE_ORIENTATION ||
                 getState() == State.WAITING_FOR_MOVE_VALIDITY;
     */
    //@ ensures \result >= 0 && \result < Board.BOARD_SIZE;
    //@ pure
    public int getSelectedBoardSpace() {
        return selectedBoardSpace;
    }

    // ---------------------------------------------------------------------------------------------

    //@ signals (NoSuchPlayerException e) !getPlayers().containsKey(playerName);
    //@ ensures getCurrentTurnPlayer().equals(getPlayers().get(playerName));
    /*@ ensures getCurrentTurnPlayer().equals(getLocalPlayer()) &&
                getState().equals(State.MAKE_MOVE_DECIDE_TILE);
      @*/
    /*@ ensures !getCurrentTurnPlayer().equals(getLocalPlayer()) &&
                getState().equals(State.WAITING_FOR_TURN);
      @*/
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

    //@ signals (NoSuchPlayerException e) !getPlayers().containsKey(playerName);
    //@ ensures getCurrentTurnPlayer().equals(getPlayers().get(playerName));
    /*@ ensures getCurrentTurnPlayer().equals(getLocalPlayer()) &&
                getState().equals(State.DECIDE_SKIP_OR_REPLACE);
      @*/
    /*@ ensures !getCurrentTurnPlayer().equals(getLocalPlayer()) &&
                getState().equals(State.WAITING_FOR_TURN);
      @*/
    public void setTurnSkip(String playerName) throws NoSuchPlayerException {
        if (!players.containsKey(playerName)) {
            throw new NoSuchPlayerException();
        }

        currentTurnPlayer = players.get(playerName);

        setChanged();

        if (currentTurnPlayer.equals(localPlayer)) {
            // Hey! It's our turn now.
            // And we have to skip or replace.
            selectedTile = null;

            currentState = State.DECIDE_SKIP_OR_REPLACE;
            notifyObservers(Change.TURN_ADVANCES_OUR_TURN);
        } else {
            // Someone else's turn.
            currentState = State.WAITING_FOR_TURN;
            notifyObservers(Change.TURN_ADVANCES);
        }
    }

    //@ signals (NoSuchPlayerException e) !getPlayers().containsKey(playerName);
    //@ requires playerName != null && hand != null;
    /*@ ensures (\forall Tile tile;
                         getPlayers().get(playerName).getTiles().contains(tile);
                         hand.contains(tile));
     @*/
    //@ ensures getPlayers().get(playerName).getTiles().size() == hand.size();
    public void setPlayerHand(String playerName, List<Tile> hand) throws NoSuchPlayerException {
        if (players.containsKey(playerName)) {
            players.get(playerName).overrideTiles(hand);
        } else {
            throw new NoSuchPlayerException();
        }
    }

    //@ requires name != null;
    //@ requires move != null;
    /*@ ensures getPlayers().containsKey(name) && 
                \old(getPlayers().get(name).getScore()) + points == 
                getPlayers().get(name).getScore();
     @*/
    /*@ ensures !getCurrentTurnPlayer().equals(getLocalPlayer()) &&
                \old(getState()).equals(State.WAITING_FOR_MOVE_VALIDITY) &&
                getState().equals(State.WAITING_FOR_TURN);
      @*/
    public void processMove(String name, Move move, int points) {
        board.placeTileDontCheckValidity(move);

        if (players.containsKey(name)) {
            players.get(name).addPoints(points);
        }

        if (name.equals(localPlayer.getName()) &&
                currentState.equals(State.WAITING_FOR_MOVE_VALIDITY)) {
            // Oooh, that was our move!
            currentState = State.WAITING_FOR_TURN;
        }

        setChanged();
        notifyObservers(Change.MOVE_MADE);
    }

    //@ requires name != null;
    //@ requires replacedTile != null;
    //@ requires replacingTile != null;
    /*@ ensures getPlayers().containsKey(name) ==>
                getPlayers().get(name).hasTileInHand(replacedTile) == false &&
                getPlayers().get(name).hasTileInHand(replacingTile);
      @*/
    /*@ ensures name.equals(getLocalPlayer().getName()) &&
                \old(getState()).equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY) &&
                getState().equals(State.WAITING_FOR_TURN);
      @*/
    public void replaceTile(String name, Tile replacedTile, Tile replacingTile) {
        if (players.containsKey(name)) {
            players.get(name).removeTile(replacedTile);
            players.get(name).addTileToHand(replacingTile);
        }

        if (name.equals(localPlayer.getName()) &&
                currentState.equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY)) {
            // That was our tile being replaced. Move is over.
            currentState = State.WAITING_FOR_TURN;
        }

        setChanged();
        notifyObservers(Change.TILE_REPLACED);
    }

    //@ requires playerName != null;
    /*@ ensures playerName.equals(getLocalPlayer().getName()) &&
                \old(getState()).equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY) &&
                getState().equals(State.WAITING_FOR_TURN);
      @*/
    public void playerSkipped(String playerName) {
        if (playerName.equals(localPlayer.getName()) &&
                currentState.equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY)) {
            // Our skip was valid.
            currentState = State.WAITING_FOR_TURN;
        }

        setChanged();
        notifyObservers(Change.PLAYER_SKIPPED);
    }

    /*@ signals (InvalidNumberException e) 
     			tileNumber < 0 || tileNumber > getLocalPlayer().getTiles().size();
      @*/
    /*@ ensures tileNumber == 0 && 
                getState().equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY) &&
                getSelectedTile() == null;
      @*/
    /*@ ensures tileNumber > 0 && tileNumber <= getLocalPlayer().getTiles().size() &&
                getState().equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY) &&
                getSelectedTile() == getLocalPlayer().getTiles().get(tileNumber - 1);
      @*/
    /*@ ensures tileNumber < 0 && tileNumber > getLocalPlayer().getTiles().size() &&
                getState().equals(State.DECIDE_SKIP_OR_REPLACE) &&
                getSelectedTile() == null;
      @*/
    public void decideSkipOrReplace(int tileNumber) throws InvalidNumberException {
        if (tileNumber == 0) {
            // Skipping
            selectedTile = null;
            currentState = State.WAITING_FOR_SKIP_REPLACE_VALIDITY;
            notifyObservers(Change.MOVE_DECISION_PROGRESS);
        } else {
            // Replace.
            if (tileNumber >= 1 && tileNumber <= localPlayer.getTiles().size()) {
                selectedTile = localPlayer.getTiles().get(tileNumber - 1);

                currentState = State.WAITING_FOR_SKIP_REPLACE_VALIDITY;
                notifyObservers(Change.MOVE_DECISION_PROGRESS);
            } else {
                // Whoops, that tile does not exist.
                selectedTile = null;
                currentState = State.DECIDE_SKIP_OR_REPLACE;

                throw new InvalidNumberException();
            }
        }

    }

    /**
     * Attempt to decide a tile to use from the hand.
     * If successful, the model will continue to the MAKE_MOVE_DECIDE_BOARD_SPACE state.
     *
     * @param tileNumber The tile to select from the hand.
     */
    /*@ signals (InvalidNumberException e) 
		tileNumber < 0 || tileNumber >= getLocalPlayer().getTiles().size();
	@*/
	/*@ ensures tileNumber >= 0 && tileNumber < getLocalPlayer().getTiles().size() &&
	    	    getState().equals(State.MAKE_MOVE_DECIDE_BOARD_SPACE) &&
	    	    getSelectedTile() == getLocalPlayer().getTiles().get(tileNumber);
	@*/
	/*@ ensures tileNumber < 0 && tileNumber >= getLocalPlayer().getTiles().size() &&
	    	    getState().equals(State.MAKE_MOVE_DECIDE_TILE) &&
	    	    getSelectedTile() == null;
	@*/
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

    /*@ signals (InvalidNumberException e) 
				spaceId < 0 || spaceId >= Board.BOARD_SIZE;
	@*/
	/*@ ensures spaceId >= 0 && spaceId < Board.BOARD_SIZE &&
	    	    getState().equals(State.MAKE_MOVE_DECIDE_BOARD_SPACE) &&
	    	    getSelectedBoardSpace() == spaceId;
	@*/
	/*@ ensures spaceId < 0 && spaceId >= Board.BOARD_SIZE &&
	    	    getState().equals(State.MAKE_MOVE_DECIDE_BOARD_SPACE) &&
	    	    getSelectedBoardSpace() == -1;
	@*/
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

    /*@ signals (InvalidNumberException e) 
				orientationNumber < 0 || orientationNumber >= 3;
	@*/
	/*@ ensures getState().equals(State.WAITING_FOR_MOVE_VALIDITY) && 
	    	    ((orientationNumber == 0 && getSelectedTile() == 
	    	        \old(getSelectedTile()) ||
	    	    (orientationNumber == 1 && getSelectedTile() == 
	    	        \old(getSelectedTile()).rotate120()) ||
	    	    (orientationNumber == 1 && getSelectedTile() == 
	    	        \old(getSelectedTile()).rotate240())));
	@*/
	/*@ ensures orientationNumber < 0 && orientationNumber >= 3 &&
			    getState().equals(State.MAKE_MOVE_DECIDE_ORIENTATION) &&
			    getSelectedTile() == \old(getSelectedTile());
	@*/
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

    /*@ ensures \old(getState()).equals(State.WAITING_FOR_MOVE_VALIDITY) &&
                getState().equals(State.MAKE_MOVE_DECIDE_TILE);
        ensures \old(getState()).equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY) &&
                getState().equals(State.DECIDE_SKIP_OR_REPLACE);
     @*/
    public void invalidMoveAttempted() {
        // Whoops, that move was invalid.
        if (currentState.equals(State.WAITING_FOR_MOVE_VALIDITY)) {
            // Clear out the selections.
            selectedTile = null;
            selectedBoardSpace = -1;

            // Try the selections again.
            currentState = State.MAKE_MOVE_DECIDE_TILE;
            setChanged();
            notifyObservers(Change.INVALID_MOVE_ATTEMPTED);
        } else if (currentState.equals(State.WAITING_FOR_SKIP_REPLACE_VALIDITY)) {
            // Clear out the selection.
            selectedTile = null;

            // Try the selection again.
            currentState = State.DECIDE_SKIP_OR_REPLACE;
            setChanged();
            notifyObservers(Change.INVALID_MOVE_ATTEMPTED);
        }
    }
}
