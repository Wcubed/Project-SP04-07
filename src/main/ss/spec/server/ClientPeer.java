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

    /**
     * Enum to denote the current state the client is in.
     * The first word indicates who we are waiting for to further the state.
     * Other threads can send messages, they just shouldn't touch the state,
     * or do something that would be invalid in this state.
     * <p>
     * PEER: means that we are waiting for a message from the peer.
     * It is the responsibility of the peer thread to further the state.
     * LOBBY: means we are waiting for an action or verification from the lobby.
     * It is the responsibility of the lobby thread to further the state.
     * GAME: means we are waiting for an action from the game thread.
     * </p>
     */
    public enum State {
        PEER_AWAITING_CONNECT_MESSAGE,
        LOBBY_VERIFY_NAME,
        PEER_AWAITING_GAME_REQUEST,
        // Client has requested a game, but has not been sent a `waiting` message yet.
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

    public ClientPeer(Connection connection, boolean verbose) {
        super(connection, verbose);

        name = null;
        this.supportsChat = false;
        this.chatMessages = new LinkedList<>();

        state = State.PEER_AWAITING_CONNECT_MESSAGE;
        requestedPlayerAmount = 0;
    }

    public ClientPeer(Connection connection) {
        this(connection, false);
    }


    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    public int getRequestedPlayerAmount() {
        return requestedPlayerAmount;
    }

    public Move getProposedMove() {
        return proposedMove;
    }

    /**
     * Returns true if the peer wants to skip, false if they want to replace.
     *
     * @return true if the peer wants to skip, false if they want to replace.
     */
    public boolean wantsToSkip() {
        return proposedReplaceTile == null;
    }

    public Tile getProposedReplaceTile() {
        return proposedReplaceTile;
    }

    /**
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
    private void parseConnectMessage(Scanner message)
            throws InvalidCommandException {

        if (getState() != State.PEER_AWAITING_CONNECT_MESSAGE) {
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

    private void parseSkipMessage(Scanner message) throws InvalidCommandException {
        if (getState() != State.PEER_DECIDE_SKIP) {
            throw new InvalidCommandException("Not expecting a skip message.");
        }

        // An exchange tile of `null` means we want to skip.
        proposedReplaceTile = null;
        state = State.GAME_VERIFY_SKIP;
    }

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

    private void parseChatMessage(Scanner message) {
        if (message.hasNextLine()) {
            // Add the chat message to the message queue.
            // Remove unnecessary spaces.
            chatMessages.addLast(message.nextLine().trim());
        }
    }


    /**
     * Called by the Lobby to signal to the client that the chosen name is valid.
     */
    public void acceptName() {
        if (getState() == State.LOBBY_VERIFY_NAME) {
            state = State.PEER_AWAITING_GAME_REQUEST;

            // Let the client know everything is ok.
            sendWelcomeMessage();
        }
    }

    /**
     * Called by the Lobby to signal to the client that the chosen name is invalid.
     */
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
     * Called by the Lobby to signal we are waiting for more players.
     */
    void signalWaitingForPlayers(List<String> names) {
        sendWaitingMessage(names);
        state = State.LOBBY_WAITING_FOR_PLAYERS;
    }


    /**
     * Called by the game to signal we are now waiting for our turn.
     */
    void awaitTurn() {
        state = State.GAME_AWAITING_TURN;
    }

    /**
     * Called by the game to let us know we are waiting for the client to send a move message.
     */
    public void clientDecideMove() {
        state = State.PEER_DECIDE_MOVE;
    }

    public void clientDecideSkip() {
        state = State.PEER_DECIDE_SKIP;
    }

    /**
     * Called when the client returns from a game to the lobby.
     * The peer can now sent a new request for a game.
     */
    void returningToLobby() {
        state = State.PEER_AWAITING_GAME_REQUEST;
    }

    public void invalidMove() {
        clientDecideMove();
        sendInvalidMoveError();
    }

    // ---- Messages -------------------------------------------------------------------------------

    private void sendWelcomeMessage() {
        // We support the chat extension.
        sendMessage("welcome chat");
    }

    public void sendChatMessage(String name, String message) {
        if (supportsChat) {
            sendMessage("chat " + name + " " + message);
        }
    }

    public void sendWaitingMessage(List<String> names) {
        sendMessage("waiting" + convertNameListToProtocol(names));
    }

    public void sendStartMessage(List<String> names) {
        sendMessage("start with" + convertNameListToProtocol(names));
    }

    public void sendOrderMessage(List<String> names) {
        sendMessage("order" + convertNameListToProtocol(names));
    }

    public void sendMoveMessage(String playerName, Move move, int points) {
        sendMessage("move " +
                playerName + " " +
                move.getTile().encode() + " " +
                move.getIndex() + " " +
                points);
    }

    public void sendSkipMessage(String playerName) {
        sendMessage("skip " + playerName);
    }

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

    public void sendPlayerLeftMessage(String playerName) {
        sendMessage("player " + playerName + " left");
    }


    public void sendInvalidNameError() {
        sendMessage(INVALID_NAME_ERROR_MESSAGE);
    }

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
