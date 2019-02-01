package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.AbstractPeer;
import ss.spec.networking.Connection;
import ss.spec.networking.DecodeException;
import ss.spec.networking.InvalidCommandException;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ClientPeer extends AbstractPeer {

    private String name;
    private int requestedPlayerAmount;

    private ClientState state;

    /**
     * The move the peer wants to make.
     * TODO: there is probably a better way to do this:
     * Is only valid when getState() == ClientState.GAME_VERIFY_MOVE.
     */
    private Move proposedMove;

    /**
     * The tile we propose to replace.
     * TODO: there is probably a better way to do this:
     * `null` if we want to skip.
     * Is only valid when getState() == ClientState.GAME_VERIFY_SKIP.
     */
    private Tile proposedReplaceTile;

    public ClientPeer(Connection connection) {
        super(connection);

        name = null;
        state = ClientState.PEER_AWAITING_CONNECT_MESSAGE;
        requestedPlayerAmount = 0;
    }


    public String getName() {
        return name;
    }

    public ClientState getState() {
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

    // ---------------------------------------------------------------------------------------------

    @Override
    public void handleReceivedMessage(String message) {
        // TODO: Nice printing of received messages.
        System.out.println("Client \'" + name + "\' sent: \'" + message + "\'");

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
                    default:
                        // We don't know this command.
                        // TODO: logging.
                        throw new InvalidCommandException("Unknown command: " + command + ".");
                }
            } catch (InvalidCommandException e) {
                // TODO: logging.
                System.out.println(e.getMessage());
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

        if (getState() != ClientState.PEER_AWAITING_CONNECT_MESSAGE) {
            throw new InvalidCommandException("Not expecting a connect message.");
        }

        if (!message.hasNext()) {
            // Whoops, `connect` message with no name.
            // TODO: Proper logging.
            throw new InvalidCommandException("Connect message does not have a name.");
        }

        String newName = message.next();
        // Wait for the lobby to verify the given name.
        state = ClientState.LOBBY_VERIFY_NAME;

        // We cannot check for spaces in the name, because a space means we start
        // with the list of extensions.

        // TODO: Proper logging.
        System.out.println("Client connected with name: " + newName);

        this.name = newName;

        // TODO: Proper logging.
        // TODO: parse [extensions]
    }


    private void parseRequestMessage(Scanner message) throws InvalidCommandException {
        if (getState() != ClientState.PEER_AWAITING_GAME_REQUEST) {
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
                state = ClientState.LOBBY_START_WAITING_FOR_PLAYERS;
            } else {
                throw new InvalidCommandException("Can only request 2 to 4 players.");
            }

        } catch (InputMismatchException e) {
            // No integer after the `request` message.
            throw new InvalidCommandException("Request message does not have a number.");
        }
    }

    private void parseMoveMessage(Scanner message) throws InvalidCommandException {
        if (getState() != ClientState.PEER_DECIDE_MOVE) {
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
        state = ClientState.GAME_VERIFY_MOVE;
    }

    private void parseSkipMessage(Scanner message) throws InvalidCommandException {
        if (getState() != ClientState.PEER_DECIDE_SKIP) {
            throw new InvalidCommandException("Not expecting a skip message.");
        }

        // An exchange tile of `null` means we want to skip.
        proposedReplaceTile = null;
        state = ClientState.GAME_VERIFY_SKIP;
    }

    private void parseExchangeMessage(Scanner message) throws InvalidCommandException {
        if (getState() != ClientState.PEER_DECIDE_SKIP) {
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
        state = ClientState.GAME_VERIFY_SKIP;
    }


    /**
     * Called by the Lobby to signal to the client that the chosen name is valid.
     */
    public void acceptName() {
        if (getState() == ClientState.LOBBY_VERIFY_NAME) {
            state = ClientState.PEER_AWAITING_GAME_REQUEST;

            // Let the client know everything is ok.
            sendWelcomeMessage();
        }
        // TODO: Do we want to signal an inconsistent state?
    }

    /**
     * Called by the Lobby to signal to the client that the chosen name is invalid.
     */
    public void rejectName() {
        if (getState() == ClientState.LOBBY_VERIFY_NAME) {
            state = ClientState.PEER_AWAITING_CONNECT_MESSAGE;
            // Clear the name.
            name = null;

            // Let the client know that this name is not acceptable.
            sendInvalidNameError();
        }
        // TODO: Do we want to signal an inconsistent state?
    }

    /**
     * Called by the Lobby to signal we are waiting for more players.
     */
    void signalWaitingForPlayers(List<String> names) {
        sendWaitingMessage(names);
        state = ClientState.LOBBY_WAITING_FOR_PLAYERS;
        // TODO: Do we want to signal an inconsistent state?
    }


    /**
     * Called by the game to signal we are now waiting for our turn.
     */
    void awaitTurn() {
        state = ClientState.GAME_AWAITING_TURN;
    }

    /**
     * Called by the game to let us know we are waiting for the client to send a move message.
     */
    public void clientDecideMove() {
        state = ClientState.PEER_DECIDE_MOVE;
    }

    public void clientDecideSkip() {
        state = ClientState.PEER_DECIDE_SKIP;
    }

    /**
     * Called when the client returns from a game to the lobby.
     * The peer can now sent a new request for a game.
     */
    void returningToLobby() {
        state = ClientState.PEER_AWAITING_GAME_REQUEST;
    }

    public void invalidMove() {
        clientDecideMove();
        sendInvalidMoveError();
    }

    // ---- Messages -------------------------------------------------------------------------------


    private void sendWelcomeMessage() {
        // TODO: Send the supported extensions (if any).
        sendMessage("welcome");
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
