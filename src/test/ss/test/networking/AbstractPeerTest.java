package ss.test.networking;

import org.junit.jupiter.api.Test;
import ss.spec.networking.ClientPeer;
import ss.spec.networking.DeadConnectionException;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractPeerTest {

    @Test
    void createWithDeadConnection() {
        MockConnection connection = new MockConnection();
        connection.setIsDead(true);

        ClientPeer peer = new ClientPeer(connection);

        assertFalse(peer.isPeerConnected());
        assertThrows(DeadConnectionException.class, () -> peer.sendMessage("Bla"));
    }

    @Test
    void connectThenDisconnect() {
        MockConnection connection = new MockConnection();
        ClientPeer peer = new ClientPeer(connection);

        assertTrue(peer.isPeerConnected());
        try {
            peer.sendMessage("Test");
        } catch (DeadConnectionException e) {
            fail("Client threw `DeadConnectionException` when it shouldn't have.");
        }

        // Kill the connection.
        connection.killConnection();

        // The peer should not have noticed yet.
        assertTrue(peer.isPeerConnected());

        assertThrows(DeadConnectionException.class, () -> peer.sendMessage("Bla"));

        // Now it has noticed.
        assertFalse(peer.isPeerConnected());
    }

    @Test
    void sendMessages() {
        MockConnection connection = new MockConnection();
        ClientPeer peer = new ClientPeer(connection);

        try {
            peer.sendMessage("hello world!");
        } catch (DeadConnectionException e) {
            fail("Client threw `DeadConnectionException` when it shouldn't have.");
        }

        assertEquals("hello world!", connection.readSentMessage());
    }

    @Test
    void sendInvalidCommandError() {
        MockConnection connection = new MockConnection();
        ClientPeer peer = new ClientPeer(connection);

        try {
            peer.sendInvalidCommandError();
        } catch (DeadConnectionException e) {
            fail("Client threw `DeadConnectionException` when it shouldn't have.");
        }

        assertEquals("invalid command", connection.readSentMessage());
    }
}
