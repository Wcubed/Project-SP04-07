package ss.test.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.ClientPeer;
import ss.spec.networking.ClientState;

import java.util.ArrayList;
import java.util.HashMap;

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
        assertEquals(ClientState.PEER_AWAITING_CONNECT_MESSAGE, peer.getState());
        assertEquals(0, peer.getRequestedPlayerAmount());
    }

    @Test
    void handleConnectMessage() {
        String name = "MyName";

        peer.handleReceivedMessage("connect " + name);

        assertEquals(name, peer.getName());
        assertEquals(ClientState.LOBBY_VERIFY_NAME, peer.getState());

        // Sending the same command again should be invalid, and have no effect.
        String differentName = "anotherName";
        peer.handleReceivedMessage("connect " + differentName);

        assertEquals(name, peer.getName());
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Reject the name.
        peer.rejectName();

        assertNull(peer.getName());
        assertEquals(ClientState.PEER_AWAITING_CONNECT_MESSAGE, peer.getState());
        assertEquals(ClientPeer.INVALID_NAME_ERROR_MESSAGE, connection.readSentMessage());

        // Send a new name, and this time accept it.
        peer.handleReceivedMessage("connect " + name);
        peer.acceptName();

        assertEquals(name, peer.getName());
        assertEquals(ClientState.PEER_AWAITING_GAME_REQUEST, peer.getState());
        assertEquals("welcome", connection.readSentMessage());
    }

    @Test
    void handleInvalidCommands() {
        peer.handleReceivedMessage("SJDKFSLDFLKsjdksfls");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("2398393");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Connect without name should be invalid.
        peer.handleReceivedMessage("connect");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("null");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
    }

    @Test
    void handleValidRequestMessage() {
        // Send the message in an invalid state.
        peer.handleReceivedMessage("request 3");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Get the peer into the right state.
        peer.handleReceivedMessage("connect Bob");
        peer.acceptName();
        connection.readSentMessage();

        // The message should now be okay.
        peer.handleReceivedMessage("request 3");
        assertEquals(3, peer.getRequestedPlayerAmount());
        assertEquals(ClientState.LOBBY_START_WAITING_FOR_PLAYERS, peer.getState());
        assertNull(connection.readSentMessage());
    }

    @Test
    void handleInvalidRequestMessage() {
        // Send invalid message in an invalid state.
        peer.handleReceivedMessage("request 16");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Get the peer into the right state.
        peer.handleReceivedMessage("connect Bob");
        peer.acceptName();
        connection.readSentMessage();
        assertEquals(ClientState.PEER_AWAITING_GAME_REQUEST, peer.getState());

        // Send invalid messages in the right state.
        peer.handleReceivedMessage("request 16");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("request 1");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("request 5");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("request HELLO!");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("request");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        peer.handleReceivedMessage("request -1");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        assertEquals(ClientState.PEER_AWAITING_GAME_REQUEST, peer.getState());

    }


    // ---- Messages -------------------------------------------------------------------------------


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
    void sendTileAndTurnMessage() {
        HashMap<String, ArrayList<Tile>> tiles = new HashMap<>();

        peer.sendTileAndTurnAnnouncement(tiles, "Clara");
        assertEquals("tiles turn Clara", connection.readSentMessage());

        ArrayList<Tile> bobTiles = new ArrayList<>();
        bobTiles.add(new Tile(Color.RED, Color.PURPLE, Color.GREEN, 3));
        tiles.put("Bob", bobTiles);

        peer.sendTileAndTurnAnnouncement(tiles, "Bob");
        assertEquals("tiles Bob RPG3 null null null turn Bob", connection.readSentMessage());

        bobTiles.add(new Tile(Color.PURPLE, Color.BLUE, Color.YELLOW, 4));

        ArrayList<Tile> albertTiles = new ArrayList<>();
        albertTiles.add(new Tile(Color.GREEN, Color.WHITE, Color.GREEN, 1));
        tiles.put("Albert", albertTiles);

        peer.sendTileAndTurnAnnouncement(tiles, "Bob");
        assertEquals("tiles Bob RPG3 PBY4 null null Albert GWG1 null null null turn Bob", connection.readSentMessage());
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
        assertEquals(ClientPeer.INVALID_NAME_ERROR_MESSAGE, connection.readSentMessage());
    }
}
