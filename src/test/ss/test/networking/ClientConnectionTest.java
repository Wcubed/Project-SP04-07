package ss.test.networking;

import org.junit.jupiter.api.Test;
import ss.spec.networking.ClientConnection;
import ss.spec.networking.DeadConnectionException;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ClientConnectionTest {

    @Test
    void createWithClosedSocket() {

        Socket socket = new Socket();

        ClientConnection connection = new ClientConnection(socket);

        assertTrue(connection.isConnectionDead());
        // Can't send stuff over a dead connection.
        assertThrows(DeadConnectionException.class, () -> connection.sendMessage("Test"));
    }
}
