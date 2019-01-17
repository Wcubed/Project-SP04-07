package ss.spec.networking;

import java.net.Socket;

public class ServerConnection extends AbstractConnection {

    public ServerConnection(Socket socket) {
        super(socket);
    }

    @Override
    public void handleMessage(String message) {
        System.out.println("Server says:" + message);
    }

    public void sendConnectMessage(String name) throws DeadConnectionException {
        sendMessage("connect " + name);

        // TODO: Include list of extensions.
    }
}
