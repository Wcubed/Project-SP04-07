package ss.spec.client;

import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.AbstractPeer;
import ss.spec.networking.Connection;
import ss.spec.networking.DecodeException;
import ss.spec.networking.InvalidCommandException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServerPeer extends AbstractPeer {

    private ClientController controller;
    private boolean serverSupportsChat;

    public ServerPeer(
            ClientController controller, Connection connection, boolean serverSupportsChat) {
        super(connection);

        this.controller = controller;
        this.serverSupportsChat = serverSupportsChat;
    }

    @Override
    public void handleReceivedMessage(String message) {
        Scanner scanner = new Scanner(message);

        if (scanner.hasNext()) {
            String command = scanner.next();

            try {
                switch (command) {
                    case "chat":
                        parseChatMessage(scanner);
                        break;
                    case "waiting":
                        parseWaitingMessage(scanner);
                        break;
                    case "start":
                        // We dont have to do anything with the "start with" message.
                        // As we will get a much more useful "order" message next :)
                        break;
                    case "order":
                        parseOrderMessage(scanner);
                        break;
                    case "tiles":
                        parseTurnMessage(scanner);
                        break;
                    case "skip":
                        // TODO: parse skip message.
                        break;
                    case "replace":
                        // TODO: parse replace message.
                        break;
                    case "move":
                        // TODO: parse move message.
                        break;
                    case "game":
                        // TODO: parse game finished message.
                        //   Show the leaderboard, and send them back to the lobby.
                        break;
                    case "player":
                        parsePlayerMessage(scanner);
                        break;
                    case "invalidCommand":
                        System.out.println("Uh oh! It looks like we sent an invalid command!");
                        break;
                    default:
                        // TODO: Invalid command or something?
                        System.out.println("Server says: " + message);
                }
            } catch (InvalidCommandException e) {
                // TODO: propperly send this to the TUI view.
                System.out.println("Received invalid command: \'" + e.getMessage() + "\'.");
                sendInvalidCommandError(e);
            }
        }
    }

    private void parseChatMessage(Scanner message) {
        if (message.hasNext()) {
            String name = message.next();

            if (message.hasNextLine()) {
                String chatMessage = message.nextLine().trim();
                controller.receiveChatMessage(name, chatMessage);
            }
        }
    }

    private void parseWaitingMessage(Scanner message) {
        List<String> names = new ArrayList<>();

        while (message.hasNext()) {
            names.add(message.next());
        }

        controller.updateWaitingForGame(names);
    }

    private void parseOrderMessage(Scanner message) throws InvalidCommandException {
        ArrayList<String> turnOrder = new ArrayList<>();

        while (message.hasNext()) {
            turnOrder.add(message.next());
        }

        try {
            controller.startGame(turnOrder);
        } catch (GameStartedWithoutUsException e) {
            // We got sent an order message that does not include ourselves.
            // We can't play in a game we don't have a turn in.
            throw new InvalidCommandException("The started game does not include us!", e);
        }
    }

    private void parseTurnMessage(Scanner message) throws InvalidCommandException {
        while (true) {
            String name;

            if (!message.hasNext()) {
                throw new InvalidCommandException("Malformed tiles message.");
            }

            String word = message.next();

            if (word.equals("turn")) {
                // No more tile messages. Let's continue with the turn message.
                break;
            } else {
                name = word;
            }

            // Parse the tiles of this player.
            ArrayList<Tile> tiles = new ArrayList<>();

            for (int i = 0; i < Player.MAX_HAND_SIZE; i++) {
                if (!message.hasNext()) {
                    throw new InvalidCommandException(
                            "Not all players have " + Player.MAX_HAND_SIZE + " tile slots");
                }

                String tileString = message.next();

                // "null" means the player has less than the maximum amount of tiles.
                if (!tileString.equals("null")) {
                    try {
                        tiles.add(Tile.decode(tileString));
                    } catch (DecodeException e) {
                        throw new InvalidCommandException(
                                "Malformed tile in the tiles message: \'" + tileString + "\'");
                    }
                }
            }

            try {
                controller.setPlayerHand(name, tiles);
            } catch (NoSuchPlayerException e) {
                // Cant set the hand of a player that does not exist.
                throw new InvalidCommandException("Malformed tiles message", e);
            }
        }

        // Who's turn is it?

        if (!message.hasNext()) {
            throw new InvalidCommandException("Tiles message does not include turn");
        }

        String name = message.next();

        try {
            controller.setTurn(name);
        } catch (NoSuchPlayerException e) {
            throw new InvalidCommandException("Malformed tiles message", e);
        }
    }

    public void parsePlayerMessage(Scanner message) throws InvalidCommandException {
        if (!message.hasNext()) {
            throw new InvalidCommandException("Malformed message starting with \'player\'");
        }

        String word = message.next();

        if (word.equals("skipped")) {
            // TODO: Parse player skipped message.
        } else {
            if (message.hasNext() && message.next().equals("left")) {
                // Player left.
                controller.playerLeftReturnToLobby(word);
            } else {
                throw new InvalidCommandException("Malformed message starting with \'player\'");
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    public void sendChatMessage(String message) {
        if (serverSupportsChat) {
            sendMessage("chat " + message);
        }
    }

    public void sendRequestMessage(int players) {
        sendMessage("request " + players);
    }


    public void sendMoveMessage(Move move) {
        sendMessage("place " +
                move.getTile().encode() + " on " +
                move.getIndex());
    }

    public void sendSkipMessage() {
        sendMessage("skip");
    }

    public void sendExchangeMessage(Tile tile) {
        sendMessage("exchange " + tile.encode());
    }
}
