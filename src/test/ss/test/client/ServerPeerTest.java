package ss.test.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.client.ServerPeer;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Move;
import ss.spec.gamepieces.Tile;
import ss.test.networking.MockConnection;

import static org.junit.jupiter.api.Assertions.*;

class ServerPeerTest {

    ServerPeer peer;
    MockConnection connection;

    @BeforeEach
    void setUp() {
        connection = new MockConnection();
        peer = new ServerPeer(null, connection, true);
    }

    // ---------------------------------------------------------------------------------------------


    @Test
    void sendChatMessage() {
        peer.sendChatMessage("Hello world!");
        assertEquals("chat Hello world!", connection.readSentMessage());

        ServerPeer noChatPeer = new ServerPeer(null, connection, false);
        noChatPeer.sendChatMessage("Oh no! We don't have chat!");
        // Should not send anything.
        assertNull(connection.readSentMessage());
    }

    @Test
    void sendRequestMessage() {
        peer.sendRequestMessage(1);
        assertEquals("request 1", connection.readSentMessage());

        peer.sendRequestMessage(2);
        assertEquals("request 2", connection.readSentMessage());

        // Not a proper number of players, but the send message shouldn't check that anyway.
        peer.sendRequestMessage(10);
        assertEquals("request 10", connection.readSentMessage());
    }

    @Test
    void sendMoveMessage() {
        peer.sendMoveMessage(new Move(
                new Tile(Color.GREEN, Color.BLUE, Color.PURPLE, 3), 4));
        assertEquals("place GBP3 on 4", connection.readSentMessage());

        peer.sendMoveMessage(new Move(
                new Tile(Color.WHITE, Color.WHITE, Color.WHITE, 4), 4));
        assertEquals("place WWW4 on 4", connection.readSentMessage());

        // Can't go sending a null tile.
        assertThrows(NullPointerException.class, () ->
                peer.sendMoveMessage(new Move(null, 6)));
    }

    @Test
    void sendSkipMessage() {
        peer.sendSkipMessage();
        assertEquals("skip", connection.readSentMessage());
    }

    @Test
    void sendExchangeMessage() {
        peer.sendExchangeMessage(new Tile(Color.GREEN, Color.YELLOW, Color.PURPLE, 9));
        assertEquals("exchange GYP9", connection.readSentMessage());

        peer.sendExchangeMessage(new Tile(Color.PURPLE, Color.WHITE, Color.RED, 1));
        assertEquals("exchange PWR1", connection.readSentMessage());

        // Can't go sending a null tile.
        assertThrows(NullPointerException.class, () ->
                peer.sendExchangeMessage(null));
    }
}
