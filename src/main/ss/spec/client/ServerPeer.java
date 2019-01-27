package ss.spec.client;

import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.AbstractPeer;
import ss.spec.networking.Connection;

public class ServerPeer extends AbstractPeer {

    public ServerPeer(Connection connection) {
        super(connection);
    }

    @Override
    public void handleReceivedMessage(String message) {
        System.out.println("Server says:" + message);
    }

    // ---------------------------------------------------------------------------------------------

    public void sendConnectMessage(String name) {
        sendMessage("connect " + name);

        // TODO: Include list of extensions.
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
