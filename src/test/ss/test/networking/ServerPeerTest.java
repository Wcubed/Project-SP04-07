package ss.test.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.networking.ServerPeer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerPeerTest {

    ServerPeer peer;
    MockConnection connection;

    @BeforeEach
    void setUp() {
        connection = new MockConnection();
        peer = new ServerPeer(connection);
    }

    @Test
    void sendConnectMessage() {
        String name = "TestingName";

        peer.sendConnectMessage(name);

        assertEquals("connect " + name, connection.readSentMessage());
    }
}
