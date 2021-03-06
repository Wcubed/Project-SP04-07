package ss.test.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Tile;
import ss.spec.server.ClientPeer;
import ss.test.networking.MockConnection;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(ClientPeer.State.PEER_AWAITING_CONNECT_MESSAGE, peer.getState());
        assertEquals(0, peer.getRequestedPlayerAmount());
    }

    @Test
    void handleConnectMessage() {
        String name = "MyName";

        peer.handleReceivedMessage("connect " + name);

        assertEquals(name, peer.getName());
        assertEquals(ClientPeer.State.LOBBY_VERIFY_NAME, peer.getState());

        // Sending the same command again should be invalid, and have no effect.
        String differentName = "anotherName";
        peer.handleReceivedMessage("connect " + differentName);

        assertEquals(name, peer.getName());
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Reject the name.
        peer.rejectName();

        assertNull(peer.getName());
        assertEquals(ClientPeer.State.PEER_AWAITING_CONNECT_MESSAGE, peer.getState());
        assertEquals(ClientPeer.INVALID_NAME_ERROR_MESSAGE, connection.readSentMessage());

        // Send a new name, and this time accept it.
        peer.handleReceivedMessage("connect " + name);
        peer.acceptName();

        assertEquals(name, peer.getName());
        assertEquals(ClientPeer.State.PEER_AWAITING_GAME_REQUEST, peer.getState());
        assertEquals("welcome chat", connection.readSentMessage());
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
        // Send the message in an invalid ClientPeer.State.
        peer.handleReceivedMessage("request 3");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Get the peer into the right ClientPeer.State.
        peer.handleReceivedMessage("connect Bob");
        peer.acceptName();
        connection.readSentMessage();

        // The message should now be okay.
        peer.handleReceivedMessage("request 3");
        assertEquals(3, peer.getRequestedPlayerAmount());
        assertEquals(ClientPeer.State.LOBBY_START_WAITING_FOR_PLAYERS, peer.getState());
        assertNull(connection.readSentMessage());
    }

    @Test
    void handleInvalidRequestMessage() {
        // Send invalid message in an invalid ClientPeer.State.
        peer.handleReceivedMessage("request 16");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Get the peer into the right ClientPeer.State.
        peer.handleReceivedMessage("connect Bob");
        peer.acceptName();
        connection.readSentMessage();
        assertEquals(ClientPeer.State.PEER_AWAITING_GAME_REQUEST, peer.getState());

        // Send invalid messages in the right ClientPeer.State.
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

        assertEquals(ClientPeer.State.PEER_AWAITING_GAME_REQUEST, peer.getState());
    }

    @Test
    void handleMoveMessage() {
        // Not allowed to make a move yet.
        peer.handleReceivedMessage("place RGB2 on 12");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Now we are allowed to make a move.
        peer.clientDecideMove();
        peer.handleReceivedMessage("place RGB4 on 12");
        assertEquals(ClientPeer.State.GAME_VERIFY_MOVE, peer.getState());
        assertEquals(
                new Tile(Color.RED, Color.GREEN, Color.BLUE, 4),
                peer.getProposedMove().getTile());
        assertEquals(12, peer.getProposedMove().getIndex());

        // Make another move.
        peer.clientDecideMove();
        peer.handleReceivedMessage("place PPP2 on 3");
        assertEquals(ClientPeer.State.GAME_VERIFY_MOVE, peer.getState());
        assertEquals(
                new Tile(Color.PURPLE, Color.PURPLE, Color.PURPLE, 2),
                peer.getProposedMove().getTile());
        assertEquals(3, peer.getProposedMove().getIndex());

        peer.clientDecideMove();

        // Mangled messages.
        peer.handleReceivedMessage("place");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("place on ");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("place RGB2 on");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("place WWW1 on -10");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("place on RRR4 12");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
    }

    @Test
    void handleSkipMessage() {
        // Not allowed to skip.
        peer.handleReceivedMessage("skip");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Now we are allowed to skip.
        peer.clientDecideSkip();
        peer.handleReceivedMessage("skip");
        assertEquals(ClientPeer.State.GAME_VERIFY_SKIP, peer.getState());
        assertNull(peer.getProposedReplaceTile());
        assertTrue(peer.wantsToSkip());

        // Mangled message
        peer.clientDecideSkip();
        peer.handleReceivedMessage("skipbldk");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
    }

    @Test
    void handleExchangeMessage() {
        // Not allowed to exchange.
        peer.handleReceivedMessage("exchange RGB2");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());

        // Now we are allowed to exchange.
        peer.clientDecideSkip();
        peer.handleReceivedMessage("exchange RGB2");
        assertEquals(ClientPeer.State.GAME_VERIFY_SKIP, peer.getState());
        assertEquals(new Tile(Color.RED, Color.GREEN, Color.BLUE, 2),
                peer.getProposedReplaceTile());
        assertFalse(peer.wantsToSkip());

        peer.clientDecideSkip();

        // Mangled messages.
        peer.handleReceivedMessage("exchange");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("exchange R");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("exchange blabla");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("exchange       ");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("exchange !");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
        peer.handleReceivedMessage("exchange PRT-1");
        assertEquals(ClientPeer.INVALID_COMMAND_ERROR_MESSAGE, connection.readSentMessage());
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

        // Not being able to replace a tile can happen.
        peer.sendReplaceMessage("Jack", previous, null);
        assertEquals("replace Jack RGW6 with null", connection.readSentMessage());
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
