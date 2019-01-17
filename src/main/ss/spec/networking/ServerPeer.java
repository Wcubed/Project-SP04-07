package ss.spec.networking;

public class ServerPeer extends AbstractPeer {

    public ServerPeer(Connection connection) {
        super(connection);
    }

    @Override
    public void parseMessage(String message) {
        System.out.println("Server says:" + message);
    }

    public void sendConnectMessage(String name) throws DeadConnectionException {
        sendMessage("connect " + name);

        // TODO: Include list of extensions.
    }
}
