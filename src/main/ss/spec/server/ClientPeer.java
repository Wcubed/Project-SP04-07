package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.AbstractPeer;
import ss.spec.networking.Connection;
import ss.spec.networking.DecodeException;
import ss.spec.networking.InvalidCommandException;

import java.util.*;

public class ClientPeer extends AbstractPeer {
    //@ invariant getName() != null;
    //@ invariant getState() != null;
	/*@ invariant getState().equals(State.LOBBY_START_WAITING_FOR_PLAYERS) &&
	              getRequestedPlayerAmount() <= 2 &&
	              getRequestedPlayerAmount() >= 4;
      @*/
	/*@ invariant getState().equals(State.GAME_VERIFY_MOVE) &&
                  getProposedMove() != null;
      @*/

    /**
     * Enum to denote the current state the client is in.
     * The first word indicates who we are waiting for to further the state.
     * Other threads can send messages, they just shouldn't touch the state,
     * or do something that would be invalid in this state.
     * <p>
     * PEER: means that we are waiting for a message from the peer.
     * It is the responsibility of the peer thread to further the state.
     * <p>
     * LOBBY: means we are waiting for an action or verification from the lobby.
     * It is the responsibility of the lobby thread to further the state.
     * <p>
     * GAME: means we are waiting for an action from the game thread.
     * </p>
     */
    public enum State {
        PEER_AWAITING_CONNECT_MESSAGE,
        LOBBY_VERIFY_NAME,
        PEER_AWAITING_GAME_REQUEST,
        /**
         * Client has requested a game, but has not been sent a `waiting` message yet.
         */
        LOBBY_START_WAITING_FOR_PLAYERS,
        LOBBY_WAITING_FOR_PLAYERS,

        GAME_AWAITING_TURN,
        PEER_DECIDE_MOVE,
        GAME_VERIFY_MOVE,
        PEER_DECIDE_SKIP,
        GAME_VERIFY_SKIP,
    }

    private String name;

    private boolean supportsChat;
    private final LinkedList<String> chatMessages;

    private int requestedPlayerAmount;

    private State state;

    /**
     * The move the peer wants to make.
     * Is only valid when getState() == ClientPeer.State.GAME_VERIFY_MOVE.
     */
    private Move proposedMove;

    /**
     * The tile we propose to replace.
     * `null` if we want to skip.
     * Is only valid when getState() == ClientPeer.State.GAME_VERIFY_SKIP.
     */
    private Tile proposedReplaceTile;

    //@ requires connection != null;
    //@ ensures isPeerConnected() == !connection.isDead();
    //@ ensures getState().equals(State.PEER_AWAITING_CONNECT_MESSAGE);
    // TODO: something with the chat here.
    public ClientPeer(Connection connection, boolean verbose) {
        super(connection, verbose);

        name = null;
        this.supportsChat = false;
        this.chatMessages = new LinkedList<>();

        state = State.PEER_AWAITING_CONNECT_MESSAGE;
        requestedPlayerAmount = 0;
    }

    //@ requires connection != null;
    //@ ensures isPeerConnected() == !connection.isDead();
    //@ ensures getState().equals(State.PEER_AWAITING_CONNECT_MESSAGE);
    // TODO: copy the other stuff from the 1st constructor.
    public ClientPeer(Connection connection) {
        this(connection, false);
    }

    /**
     * The name this client has chosen.
     *
     * @return The client's name.
     */
    /*@ ensures \result == null && 
                getState().equals(State.PEER_AWAITING_CONNECT_MESSAGE);
      @ ensures \result != null &&
                !getState().equals(State.PEER_AWAITING_CONNECT_MESSAGE);
      @*/
    //@ pure
    public String getName() {
        return name;
    }

    /**
     * The current state of the client. Refer to the `State` documentation for more info.
     *
     * @return The current state of the client.
     */
    //@ pure
    public State getState() {
        return state;
    }

    /**
     * The amount of players this client wants to play a game with. This is used by the `Lobby`
     * class to put the client into the correct waiting list.
     * <p>
     * Is only valid when the client's state is: State.LOBBY_START_WAITING_FOR_PLAYERS.
     *
     * @return The amount of players this client wants to play a game with. In the range [2-4]. or
     * -1 when called at an invalid moment (you shouldn't).
     */
    //@ requires getState().equals(State.LOBBY_START_WAITING_FOR_PLAYERS);
    //@ pure
    public int getRequestedPlayerAmount() {
        return requestedPlayerAmount;
    }

    /**
     * Get the move this client wants to make. The move is to be verified by the `Game` class.
     * <p>
     * Is only valid when the client's state is: State.GAME_VERIFY_MOVE.
     *
     * @return The move this client wants to make. Or `null` when called at an invalid moment.
     */
    //@ requires getState().equals(State.GAME_VERIFY_MOVE);
    //@ pure
    public Move getProposedMove() {
        return proposedMove;
    }

    /**
     * Returns true if the peer wants to skip, false if they want to replace a tile.
     * This choice is then to be verified by the `Game` class.
     * <p>
     * Is only valid when the client's state is: State.GAME_VERIFY_SKIP.
     *
     * @return true if the peer wants to skip, false if they want to replace a tile.
     */
    //@ requires getState().equals(State.GAME_VERIFY_SKIP);
    //@ ensures \result == (getProposedReplaceTile() == null);
    //@ pure
    public boolean wantsToSkip() {
        return proposedReplaceTile == null;
    }

    /**
     * Returns the tile this player proposes to replace. This choice can then be verified by the
     * `Game` class.
     * <p>
     * Is only valid when the client's state is: State.GAME_VERIFY_SKIP
     *
     * @return The tile this player wants to replace. Or `null` when called at an invalid moment.
     */
    //@ requires getState().equals(State.GAME_VERIFY_SKIP);
    //@ pure
    public Tile getProposedReplaceTile() {
        return proposedReplaceTile;
    }

    /**
     * Gives the next chat message in this client's queue of chat messages.
     * Returns `null` if there are currently no chat messages waiting.
     *
     * @return The next chat message in the queue. `null` if the queue is empty.
     */
    public String getNextChatMessage() {
        try {
            return chatMessages.removeFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * This method is called by the inherited `run()` method of `AbstractPeer` when a new message
     * has been received.
     * This method parses the first word of the message, and identifies the command based on that
     * word. It then calls the correct parsing function for that command.
     * <p>
     * If the command cannot be parsed, either by this function or one of the called parsers,
     * then a message will be sent to the client stating that it sent an invalid command.
     *
     * @param message The message that was received.
     */
    @Override
    public void handleReceivedMessage(String message) {
        if (verbosePrinting()) {
            System.out.println("Client \'" + name + "\' sent: \'" + message + "\'");
        }

        Scanner scanner = new Scanner(message);

        if (scanner.hasNext()) {
            String command = scanner.next();

            try {
                switch (command) {
                    case "connect":
                        parseConnectMessage(scanner);
                        break;
                    case "request":
                        parseRequestMessage(scanner);
                        break;
                    case "place":
                        parseMoveMessage(scanner);
                        break;
                    case "skip":
                        parseSkipMessage(scanner);
                        break;
                    case "exchange":
                        parseExchangeMessage(scanner);
                        break;
                    case "chat":
                        parseChatMessage(scanner);
                        break;
                    case "invalidCommand":
                        System.out.println("Uh oh! It looks like we sent an invalid command!");
                        break;
                    default:
                        // We don't know this command.
                        throw new InvalidCommandException("Unknown command: " + command + ".");
                }
            } catch (InvalidCommandException e) {
                System.out.println("Invalid command: \'" + e.getMessage() + "\'.");
                sendInvalidCommandError(e);
            }
        } else {
            sendInvalidCommandError(new InvalidCommandException("Empty command received."));
        }
    }

    /**
     * Parses the connect message.
     * If the message parses, it sets the name value and the extensions.
     *
     * @param message A scanner over the message that needs parsing.
     */
    /*@ signals (InvalidCommandException e)
                !\old(getState()).equals(State.PEER_AWAITING_CONNECT_MESSAGE) ||
                !message.hasNext();
      @ ensures \old(getState()).equals(State.PEER_AWAITING_CONNECT_MESSAGE) &&
                message.hasNext() &&
                getState().equals(State.LOBBY_VERIFY_NAME) &&
                getName() != null;
      @*/
    private void parseConnectMessage(Scanner message)
            throws InvalidCommandException {

        if (!getState().equals(State.PEER_AWAITING_CONNECT_MESSAGE)) {
            throw new InvalidCommandException("Not expecting a connect message.");
        }

        if (!message.hasNext()) {
            // Whoops, `connect` message with no name.
            throw new InvalidCommandException("Connect message does not have a name.");
        }

        String newName = message.next();
        // Wait for the lobby to verify the given name.
        state = State.LOBBY_VERIFY_NAME;

        // We cannot check for spaces in the name, because a space means we start
        // with the list of extensions.

        System.out.println("Client connected with name: " + newName);

        this.name = newName;

        // See if the client supports the chat extension.
        while (message.hasNext()) {
            if (message.next().equals("chat")) {
                this.supportsChat = true;
            }
        }
    }


    /**
     * Parses a request message and remembers the amount of players requested.
     * If the message parses correctly, the state advances to
     * `State.LOBBY_START_WAITING_FOR_PLAYERS`.
     * <p>
     * Can't @ensure anything in the JML because that requires calls to message.nextInt(),
     * which is not a pure method.
     *
     * @param message The message to parse.
     * @throws InvalidCommandException When the message cannot be parsed as a `request` command.
     */
    /*@ signals (InvalidCommandException e)
	    		!\old(getState()).equals(State.PEER_AWAITING_GAME_REQUEST) ||
	    		!message.hasNext();
	  @ requires message != null;
	  @*/
    private void parseRequestMessage(Scanner message) throws InvalidCommandException {
        if (getState() != State.PEER_AWAITING_GAME_REQUEST) {
            throw new InvalidCommandException("Not expecting a game request.");
        }

        if (!message.hasNext()) {
            throw new InvalidCommandException("Request message does not have a number.");
        }

        try {
            int amount = message.nextInt();

            // 2 to 4 players.
            if (amount >= 2 && amount <= 4) {
                requestedPlayerAmount = amount;
                state = State.LOBBY_START_WAITING_FOR_PLAYERS;
            } else {
                throw new InvalidCommandException("Can only request 2 to 4 players.");
            }

        } catch (InputMismatchException e) {
            // No integer after the `request` message.
            throw new InvalidCommandException("Request message does not have a number.");
        }
    }

    /**
     * Can't @ensure anything in the JML because that requires calls to message.next(),
     * which is not a pure method.
     *
     * @param message The message to parse.
     * @throws InvalidCommandException Thrown when the message can't be parsed propperly.
     */
    /*@ signals (InvalidCommandException e)
		        !\old(getState()).equals(State.PEER_DECIDE_MOVE) ||
		        !message.hasNext();
     @ requires message != null;
	 @*/
    private void parseMoveMessage(Scanner message) throws InvalidCommandException {
        if (getState() != State.PEER_DECIDE_MOVE) {
            throw new InvalidCommandException("Client is not allowed to make a move.");
        }

        if (!message.hasNext()) {
            throw new InvalidCommandException("Move message does not have a tile.");
        }

        Tile tile;

        try {
            tile = Tile.decode(message.next());
        } catch (DecodeException e) {
            throw new InvalidCommandException("Move message does not have a tile.", e);
        }

        if (!message.hasNext() || !message.next().equals("on")) {
            throw new InvalidCommandException("Malformed move message.");
        }

        if (!message.hasNext()) {
            throw new InvalidCommandException("Move message does not have an index.");
        }

        int index;

        try {
            index = Integer.decode(message.next());
        } catch (NumberFormatException e) {
            throw new InvalidCommandException("Move message does not have an index.", e);
        }

        if (!Board.isIdValid(index)) {
            throw new InvalidCommandException("Move message index is invalid.");
        }

        // Save the move so that the game thread can check it.
        proposedMove = new Move(tile, index);
        state = State.GAME_VERIFY_MOVE;
    }

    /*@ signals (InvalidCommandException e)
			    !\old(getState()).equals(State.PEER_DECIDE_SKIP);
      @ ensures getState().equals(State.GAME_VERIFY_SKIP);
	  @*/
    private void parseSkipMessage(Scanner message) throws InvalidCommandException {
        if (getState() != State.PEER_DECIDE_SKIP) {
            throw new InvalidCommandException("Not expecting a skip message.");
        }

        // An exchange tile of `null` means we want to skip.
        proposedReplaceTile = null;
        state = State.GAME_VERIFY_SKIP;
    }

    /**
     * Can't @ensure anything in the JML because that requires calls to message.nextInt(),
     * which is not a pure method.
     *
     * @param message The message to be parsed.
     * @throws InvalidCommandException Thrown when the message cannot be parsed propperly.
     */
    /*@ signals (InvalidCommandException e)
				!\old(getState()).equals(State.PEER_DECIDE_SKIP) ||
		        !message.hasNext();
	  @ requires message != null;
	  @*/
    private void parseExchangeMessage(Scanner message) throws InvalidCommandException {
        if (getState() != State.PEER_DECIDE_SKIP) {
            throw new InvalidCommandException("Not expecting an exchange message.");
        }

        if (!message.hasNext()) {
            throw new InvalidCommandException("Exchange message does not have a tile.");
        }

        Tile tile;

        try {
            tile = Tile.decode(message.next());
        } catch (DecodeException e) {
            throw new InvalidCommandException("Exchange message does not have a tile.", e);
        }

        proposedReplaceTile = tile;
        state = State.GAME_VERIFY_SKIP;
    }

    /**
     * Cannot ensure anything about `getNextChatMessage()` for that is not pure.
     * After calling this method with a valid message,
     * `getNextChatMessage()` will return a valid string.
     *
     * @param message The message to be parsed.
     */
    //@ requires message != null;
    private void parseChatMessage(Scanner message) {
        if (message.hasNextLine()) {
            // Add the chat message to the message queue.
            // Remove unnecessary spaces.
            chatMessages.addLast(message.nextLine().trim());
        }
    }


    /**
     * Called by the Lobby to signal to the client that the chosen name, as given by
     * `getName()`, is valid.
     * <p>
     * The client's state will continue to State.PEER_AWAITING_GAME_REQUEST.
     */
    /*@ 
	  @ ensures \old(getState()).equals(State.LOBBY_VERIFY_NAME) &&
	            getState().equals(State.PEER_AWAITING_GAME_REQUEST);
	  @*/
    public void acceptName() {
        if (getState() == State.LOBBY_VERIFY_NAME) {
            state = State.PEER_AWAITING_GAME_REQUEST;

            // Let the client know everything is ok.
            sendWelcomeMessage();
        }
    }

    /**
     * Called by the Lobby to signal to the client that the chosen name, as given by
     * `getName()`, is invalid.
     * <p>
     * The name will be cleared to `null` and the state will revert to
     * State.PEER_AWAITING_CONNECT_MESSAGE.
     */
    /*@ 
	  @ ensures \old(getState()).equals(State.LOBBY_VERIFY_NAME) &&
	            getState().equals(State.PEER_AWAITING_CONNECT_MESSAGE) &&
	            getName() == null;
	  @*/
    public void rejectName() {
        if (getState() == State.LOBBY_VERIFY_NAME) {
            state = State.PEER_AWAITING_CONNECT_MESSAGE;
            // Clear the name.
            name = null;

            // Let the client know that this name is not acceptable.
            sendInvalidNameError();
        }
    }

    /**
     * Called by the Lobby to signal this client that they are now waiting for more players to
     * join the game list, so that the game may start.
     */
    //@ ensures getState().equals(State.LOBBY_WAITING_FOR_PLAYERS);
    void signalWaitingForPlayers(List<String> names) {
        sendWaitingMessage(names);
        state = State.LOBBY_WAITING_FOR_PLAYERS;
    }


    /**
     * Called by the game to signal this client is now waiting for their turn.
     */
    //@ ensures getState().equals(State.GAME_AWAITING_TURN);
    void awaitTurn() {
        state = State.GAME_AWAITING_TURN;
    }

    /**
     * Called by the game to let the client know we are waiting for the client to send a move
     * message.
     */
    //@ ensures getState().equals(State.PEER_DECIDE_MOVE);
    public void clientDecideMove() {
        state = State.PEER_DECIDE_MOVE;
    }

    /**
     * Called by the game to let us know we are waiting for the client to decide if they want to
     * skip a turn, or replace a tile.
     */
    //@ ensures getState().equals(State.PEER_DECIDE_SKIP);
    public void clientDecideSkip() {
        state = State.PEER_DECIDE_SKIP;
    }

    /**
     * Called when the client returns from a game to the lobby.
     * The peer can now sent a new request for a game.
     */
    //@ ensures getState().equals(State.PEER_AWAITING_GAME_REQUEST);
    void returningToLobby() {
        state = State.PEER_AWAITING_GAME_REQUEST;
    }

    /**
     * Called by the game to signal that the proposed move, as given by `getProposedMove()` is
     * invalid.
     * <p>
     * Internally calls `clientDecideMove()` to signal the client they need to decide another move.
     */
    //@ ensures getState().equals(State.PEER_DECIDE_MOVE);
    public void invalidMove() {
        sendInvalidMoveError();
        clientDecideMove();
    }

    // ---- Messages -------------------------------------------------------------------------------

    private void sendWelcomeMessage() {
        // We support the chat extension.
        sendMessage("welcome chat");
    }

    /**
     * Sends a chat message to this client.
     *
     * @param playerName The name of the client who sent the chat message.
     * @param message    The message the other client sent.
     */
    public void sendChatMessage(String playerName, String message) {
        if (supportsChat) {
            sendMessage("chat " + playerName + " " + message);
        }
    }

    /**
     * Send a waiting message to the client.
     * This message includes the names of the other clients who are waiting for the same game.
     *
     * @param names The other clients who are waiting for the same game as this client.
     */
    public void sendWaitingMessage(List<String> names) {
        sendMessage("waiting" + convertNameListToProtocol(names));
    }

    /**
     * Sends the message that indicates a new game has started.
     * Includes the names of the participating clients.
     *
     * @param names The clients who are participating in this game.
     */
    public void sendStartMessage(List<String> names) {
        sendMessage("start with" + convertNameListToProtocol(names));
    }

    /**
     * Send the order in which the players will have their turns.
     *
     * @param turnOrder The order in which the players have their turns,
     *                  by the names of the players.
     */
    public void sendOrderMessage(List<String> turnOrder) {
        sendMessage("order" + convertNameListToProtocol(turnOrder));
    }

    /**
     * Sends a message to the client indicating that a player has made a valid move.
     * This is called after every valid move, also one that this client made.
     *
     * @param playerName The name of the player who made the move.
     * @param move       The move the player made.
     * @param points     The points the player gained by making this move.
     */
    public void sendMoveMessage(String playerName, Move move, int points) {
        sendMessage("move " +
                playerName + " " +
                move.getTile().encode() + " " +
                move.getIndex() + " " +
                points);
    }

    /**
     * Send a message that tells the client a player needs to skip or replace a tile.
     * This could be this client, or any other player in a game.
     *
     * @param playerName The name of the player who needs to skip or replace a tile.
     */
    public void sendSkipMessage(String playerName) {
        sendMessage("skip " + playerName);
    }

    /**
     * Sends a message to the client indicating that a player has replaced a tile from their hand.
     *
     * @param playerName  The name of the player who replaced a tile.
     * @param previous    The tile that got replaced.
     * @param replacement The replacing tile.
     */
    public void sendReplaceMessage(String playerName, Tile previous, Tile replacement) {
        // It can happen that there is no replacement left in the bag.
        String replacedWith = "null";
        if (replacement != null) {
            replacedWith = replacement.encode();
        }

        sendMessage("replace " +
                playerName + " " +
                previous.encode() +
                " with " +
                replacedWith);
    }

    /**
     * Send the message that another player left while a game was running.
     * This indicates to the client that the game is hereby over.
     *
     * @param playerName The name of the player who left the game prematurely.
     */
    public void sendPlayerLeftMessage(String playerName) {
        sendMessage("player " + playerName + " left");
    }

    /**
     * Sends a message to the client to tell them their chosen name is invalid, and that they
     * need to chose another.
     */
    public void sendInvalidNameError() {
        sendMessage(INVALID_NAME_ERROR_MESSAGE);
    }

    /**
     * Sends a message to the client to tell them their proposed move is invalid, and that they
     * need to propose another.
     */
    public void sendInvalidMoveError() {
        sendMessage(INVALID_MOVE_ERROR_MESSAGE);
    }


    /**
     * Converts a list of names into a message usable in the communication protocol.
     * Includes the space to signal the start of the list of names.
     *
     * @param names The list of names to convert.
     * @return The string to use in the protocol.
     */
    private String convertNameListToProtocol(List<String> names) {
        StringBuilder message = new StringBuilder();

        for (String n : names) {
            message.append(" ");
            message.append(n);
        }

        return message.toString();
    }
}
