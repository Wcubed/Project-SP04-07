package ss.spec.client;

import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.AbstractPeer;
import ss.spec.networking.Connection;

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


            switch (command) {
                case "chat":
                    parseChatMessage(scanner);
                    break;
                case "move":
                    // TODO: parse move.
                    break;
                // TODO: parse other messages.
                default:
                    // TODO: Invalid command or something?
                    System.out.println("Server says: " + message);
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
