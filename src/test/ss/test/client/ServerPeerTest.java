package ss.test.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.client.ServerPeer;
import ss.test.networking.MockConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerPeerTest {

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
