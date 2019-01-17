package ss.test.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.Color;
import ss.spec.Tile;
import ss.spec.networking.ClientPeer;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientPeerTest {

    ClientPeer peer;
    MockConnection connection;

    @BeforeEach
    void setUp() {
        connection = new MockConnection();
        peer = new ClientPeer(connection);
    }

    @Test
    void handleConnectMessage() {
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

    @Test
    void handleInvalidMessage() {
        peer.handleReceivedMessage("SJDKFSLDFLKsjdksfls");
        assertEquals("invalid command", connection.readSentMessage());
    }

    @Test
    void sendWaitingMessage() {
        peer.sendWaitingMessage(new ArrayList<>());
        assertEquals("waiting", connection.readSentMessage());

        ArrayList<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        names.add("Clarice");

        peer.sendWaitingMessage(names);
        assertEquals("waiting Alice Bob Clarice", connection.readSentMessage());
    }

    @Test
    void sendStartMessage() {
        peer.sendStartMessage(new ArrayList<>());
        assertEquals("start with", connection.readSentMessage());

        ArrayList<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Diane");
        names.add("Clarice");

        peer.sendStartMessage(names);
        assertEquals("start with Alice Diane Clarice", connection.readSentMessage());
    }

    @Test
    void sendOrderMessage() {
        peer.sendOrderMessage(new ArrayList<>());
        assertEquals("order", connection.readSentMessage());

        ArrayList<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Diane");
        names.add("Bob");

        peer.sendOrderMessage(names);
        assertEquals("order Alice Diane Bob", connection.readSentMessage());
    }

    @Test
    void sendSkipMessage() {
        peer.sendSkipMessage("Bob");
        assertEquals("skip Bob", connection.readSentMessage());

        peer.sendSkipMessage("John");
        assertEquals("skip John", connection.readSentMessage());
    }

    @Test
    void sendReplaceMessage() {
        Tile previous = new Tile(Color.RED, Color.GREEN, Color.WHITE, 6);
        Tile replacement = new Tile(Color.BLUE, Color.PURPLE, Color.YELLOW, 2);
        peer.sendReplaceMessage("Patrice", previous, replacement);

        assertEquals("replace Patrice RGW6 with BPY2", connection.readSentMessage());
    }

    @Test
    void sendPlayerLeftMessage() {
        peer.sendPlayerLeftMessage("Bob");
        assertEquals("player Bob left", connection.readSentMessage());

        peer.sendPlayerLeftMessage("aLiCe");
        assertEquals("player aLiCe left", connection.readSentMessage());
    }
}
