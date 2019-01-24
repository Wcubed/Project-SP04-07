package ss.test.networking;

import org.junit.jupiter.api.Test;
import ss.spec.networking.ClientPeer;
import ss.spec.networking.InvalidCommandException;

import static org.junit.jupiter.api.Assertions.*;

class AbstractPeerTest {

    @Test
    void createWithDeadConnection() {
        MockConnection connection = new MockConnection();
        connection.setIsDead(true);

        ClientPeer peer = new ClientPeer(connection);

        assertFalse(peer.isPeerConnected());
    }

    @Test
    void connectThenDisconnect() {
        MockConnection connection = new MockConnection();
        ClientPeer peer = new ClientPeer(connection);

        assertTrue(peer.isPeerConnected());

        peer.sendMessage("Test");

        assertTrue(peer.isPeerConnected());

        // Kill the connection.
        connection.killConnection();

        // The peer should not have noticed yet.
        assertTrue(peer.isPeerConnected());

        peer.sendMessage("Bla");

        // Now it has noticed.
        assertFalse(peer.isPeerConnected());
    }

    @Test
    void sendMessages() {
        MockConnection connection = new MockConnection();
        ClientPeer peer = new ClientPeer(connection);

        peer.sendMessage("hello world!");
        assertEquals("hello world!", connection.readSentMessage());
    }

    @Test
    void sendInvalidCommandError() {
        MockConnection connection = new MockConnection();
        ClientPeer peer = new ClientPeer(connection);

        peer.sendInvalidCommandError(new InvalidCommandException("Test"));

        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
    }
}
