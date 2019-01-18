package ss.test.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.Color;
import ss.spec.Tile;
import ss.spec.networking.ClientPeer;
import ss.spec.networking.ClientState;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientPeerTest {

    private ClientPeer peer;
    private MockConnection connection;

    @BeforeEach
    void setUp() {
        connection = new MockConnection();
        peer = new ClientPeer(connection);
    }

    @Test
    void initialState() {
        assertNull(peer.getName());
        assertEquals(ClientState.PEER_CONNECT_MESSAGE, peer.getState());
    }

    @Test
    void handleConnectMessage() {
        String name = "MyName";

        peer.handleReceivedMessage("connect " + name);

        assertEquals(name, peer.getName());
        assertEquals(ClientState.LOBBY_NAME_VERIFICATION, peer.getState());

        // Sending the same command again should be invalid, and have no effect.
        String differentName = "anotherName";
        peer.handleReceivedMessage("connect " + differentName);

        assertEquals(name, peer.getName());
        assertEquals("invalid command", connection.readSentMessage());

        // Reject the name.
        peer.rejectName();

        assertNull(peer.getName());
        assertEquals(ClientState.PEER_CONNECT_MESSAGE, peer.getState());
        assertEquals("invalid name", connection.readSentMessage());

        // Send a new name, and this time accept it.
        peer.handleReceivedMessage("connect " + name);
        peer.acceptName();

        assertEquals(name, peer.getName());
        assertEquals(ClientState.PEER_GAME_REQUEST, peer.getState());
        assertEquals("welcome", connection.readSentMessage());
    }

    @Test
    void handleInvalidMessage() {
        peer.handleReceivedMessage("SJDKFSLDFLKsjdksfls");
        assertEquals("invalid command", connection.readSentMessage());

        peer.handleReceivedMessage("2398393");
        assertEquals("invalid command", connection.readSentMessage());

        // Connect without name should be invalid.
        peer.handleReceivedMessage("connect");
        assertEquals("invalid command", connection.readSentMessage());
    }


    // ---------------------------------------------------------------------------------------------


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
        names.add("45DKks");
        names.add("Bob");

        peer.sendOrderMessage(names);
        assertEquals("order Alice 45DKks Bob", connection.readSentMessage());
    }

    @Test
    void sendTurnMessage() {
        peer.sendTurnMessage("diaNe");
        assertEquals("turn diaNe", connection.readSentMessage());

        peer.sendTurnMessage("38981*(#");
        assertEquals("turn 38981*(#", connection.readSentMessage());
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

    @Test
    void sendInvalidNameMessage() {
        peer.sendInvalidNameError();
        assertEquals("invalid name", connection.readSentMessage());
    }
}
