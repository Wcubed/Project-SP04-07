package ss.spec.client;

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

    public void sendConnectMessage(String name) {
        sendMessage("connect " + name);

        // TODO: Include list of extensions.
    }
}