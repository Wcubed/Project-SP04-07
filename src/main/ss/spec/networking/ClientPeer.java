package ss.spec.networking;

import ss.spec.Tile;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ClientPeer extends AbstractPeer {

    private String name;
    private int requestedPlayerAmount;
    /**
     * The state of this client.
     * Don't read this directly, always use 'getState()'.
     * This should make testing easier.
     */
    private ClientState state;

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

    @Override
    public void handleReceivedMessage(String message) {
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
                    default:
                        // We don't know this command.
                        // TODO: logging.
                        throw new InvalidCommandException("Unknown command: " + command + ".");
                }
            } catch (InvalidCommandException e) {
                // TODO: logging.
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
                state = ClientState.LOBBY_SEND_NAME_LIST;
            } else {
                throw new InvalidCommandException("Can only request 2 to 4 players.");
            }

        } catch (InputMismatchException e) {
            // No integer after the `request` message.
            throw new InvalidCommandException("Request message does not have a number.");
        }
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

    public void sendTurnMessage(String playerName) {
        sendMessage("turn " + playerName);
    }

    public void sendSkipMessage(String playerName) {
        sendMessage("skip " + playerName);
    }

    public void sendReplaceMessage(String playerName, Tile previous, Tile replacement) {
        sendMessage("replace " + playerName + " " +
                convertTileToProtocol(previous) +
                " with " +
                convertTileToProtocol(replacement));
    }

    public void sendPlayerLeftMessage(String playerName) {
        sendMessage("player " + playerName + " left");
    }

    public void sendInvalidNameError() {
        sendMessage("invalid name");
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
