package ss.test.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.networking.ClientPeer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientPeerTest {

    ClientPeer peer;
    MockConnection connection;

    @BeforeEach
    void setUp() {
        connection = new MockConnection();
        peer = new ClientPeer(connection);
    }

    @Test
    void parseConnectMessage() {
        String name = "MyName";

        peer.handleReceivedMessage("connect " + name);

        assertEquals(name, peer.getName());
        assertEquals("welcome", connection.readSentMessage());


        // Sending the same command again should be invalid, and have no effect.
        String differentName = "anotherName";
        peer.handleReceivedMessage("connect " + differentName);

        assertEquals(name, peer.getName());
        assertEquals("invalid command", connection.readSentMessage());
    }
}
