package ss.spec.networking;

import java.io.IOException;
import java.net.Socket;

public class ServerConnection extends AbstractConnection {

    public ServerConnection(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public void handleMessage(String message) {
        System.out.println("Server says:" + message);
    }
}
