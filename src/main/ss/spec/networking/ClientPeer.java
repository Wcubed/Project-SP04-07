package ss.spec.networking;

import ss.spec.Tile;

import java.util.List;
import java.util.Scanner;

public class ClientPeer extends AbstractPeer {

    private String name;
    private ClientState state;

    public ClientPeer(Connection connection) {
        super(connection);

        name = null;
        state = ClientState.PEER_CONNECT_MESSAGE;
    }

    @Override
    public void handleReceivedMessage(String message) {
        Scanner scanner = new Scanner(message);

        if (scanner.hasNext()) {
            String command = scanner.next();

            if (state == ClientState.PEER_CONNECT_MESSAGE) {
                // They have not sent the `connect <name> [extensions] message yet.`
                try {
                    parseConnectMessage(command, scanner);
                } catch (InvalidCommandException e) {
                    sendInvalidCommandError();
                }
            } else {
                // TODO: Implement the other commands.
                // For now, the rest is invalid.
                sendInvalidCommandError();
            }
        }
    }

    /**
     * Parses the connect message.
     * If the message parses, it sets the name value and the extensions.
     *
     * @param message The scanner with the message. The name should be next.
     * @param command The command of this message, if it is not `connect` this will throw an
     *                invalidCommandException.
     */
    private void parseConnectMessage(String command, Scanner message)
            throws InvalidCommandException {
        if (command.equals("connect")) {
            if (message.hasNext()) {
                String newName = message.next();
                // Wait for the lobby to verify the given name.
                state = ClientState.LOBBY_NAME_VERIFICATION;

                // We cannot check for spaces in the name, because a space means we start
                // with the list of extensions.

                // TODO: Proper logging.
                System.out.println("Client connected with name: " + newName);

                this.name = newName;
            } else {
                // Whoops, `connect` message with no name.
                // TODO: Proper logging.
                throw new InvalidCommandException();
            }

            // TODO: Proper logging.
            // TODO: parse [extensions]

        } else {
            // We are expecting a connect message here.
            // TODO: Proper logging.
            throw new InvalidCommandException();
        }

    }

    public String getName() {
        return name;
    }

    public ClientState getState() {
        return state;
    }

    /**
     * Called by the Lobby to signal to the client that the chosen name is valid.
     */
    public void acceptName() {
        if (state == ClientState.LOBBY_NAME_VERIFICATION) {
            state = ClientState.PEER_GAME_REQUEST;

            // Let the client know everything is ok.
            sendWelcomeMessage();
        }
        // TODO: Do we want to signal an inconsistent state?
    }

    /**
     * Called by the Lobby to signal to the client that the chosen name is invalid.
     */
    public void rejectName() {
        if (state == ClientState.LOBBY_NAME_VERIFICATION) {
            state = ClientState.PEER_CONNECT_MESSAGE;
            // Clear the name.
            name = null;

            // Let the client know that this name is not acceptable.
            sendInvalidNameError();
        }
        // TODO: Do we want to signal an inconsistent state?
    }

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
