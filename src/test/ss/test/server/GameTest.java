package ss.test.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Board;
import ss.spec.server.ClientPeer;
import ss.spec.server.Game;
import ss.test.gamepieces.MockTileBag;
import ss.test.networking.MockConnection;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private Game game;
    private Board board;
    private MockTileBag tileBag;

    private ClientPeer player1Alice;
    private MockConnection con1Alice;
    private ClientPeer player2Bob;
    private MockConnection con2Bob;
    private ClientPeer player3Clarice;
    private MockConnection con3Clarice;
    private ClientPeer player4Diane;
    private MockConnection con4Diane;

    @BeforeEach
    void setUp() {
        con1Alice = new MockConnection();
        con2Bob = new MockConnection();
        con3Clarice = new MockConnection();
        con4Diane = new MockConnection();

        player1Alice = new ClientPeer(con1Alice);
        player2Bob = new ClientPeer(con2Bob);
        player3Clarice = new ClientPeer(con3Clarice);
        player4Diane = new ClientPeer(con4Diane);

        player1Alice.handleReceivedMessage("connect Alice");
        player2Bob.handleReceivedMessage("connect Bob");
        player3Clarice.handleReceivedMessage("connect Clarice");
        player4Diane.handleReceivedMessage("connect Diane");

        ArrayList<ClientPeer> players = new ArrayList<>();
        players.add(player1Alice);
        players.add(player2Bob);
        players.add(player3Clarice);
        players.add(player4Diane);

        board = new Board();
        tileBag = new MockTileBag();

        game = new Game(players, board, tileBag);

        game.setUpGame();
    }

    @Test
    void startMessage() {
        String messageAlice = con1Alice.readSentMessage();

        System.out.println(messageAlice);

        assertTrue(messageAlice.contains("start with"));
        assertTrue(messageAlice.contains("Alice"));
        assertTrue(messageAlice.contains("Bob"));
        assertTrue(messageAlice.contains("Clarice"));
        assertTrue(messageAlice.contains("Diane"));

        // Bob should have gotten the same message.
        String messageBob = con2Bob.readSentMessage();

        System.out.println(messageBob);

        assertTrue(messageBob.contains("start with"));
        assertTrue(messageBob.contains("Alice"));
        assertTrue(messageBob.contains("Bob"));
        assertTrue(messageBob.contains("Clarice"));
        assertTrue(messageBob.contains("Diane"));
    }

    @Test
    void orderMessage() {
        // Remove the start messages.
        con1Alice.readSentMessage();
        con2Bob.readSentMessage();

        // We know the order of the tiles in the bag.
        // And the game class will draw tiles in the order of the received player list.
        // We can determine that this should be the resulting order of players.
        assertEquals("order Alice Diane Clarice Bob", con1Alice.readSentMessage());

        // Bob should have gotten the same message.
        assertEquals("order Alice Diane Clarice Bob", con2Bob.readSentMessage());
    }

    @Test
    void turnMessage() {
        con3Clarice.purgeSentMessages();
        con4Diane.purgeSentMessages();

        game.doSingleGameThreadIteration();

        String messageClarice = con3Clarice.readSentMessage();

        System.out.println(messageClarice);

        assertTrue(messageClarice.contains("tiles"));
        assertTrue(messageClarice.contains("Alice"));
        assertTrue(messageClarice.contains("Bob"));
        assertTrue(messageClarice.contains("Clarice"));
        assertTrue(messageClarice.contains("Diane"));
        assertTrue(messageClarice.contains("turn"));

        // Bob should have gotten the same message.
        String messageDiane = con4Diane.readSentMessage();

        System.out.println(messageDiane);

        assertTrue(messageDiane.contains("tiles"));
        assertTrue(messageDiane.contains("Alice"));
        assertTrue(messageDiane.contains("Bob"));
        assertTrue(messageDiane.contains("Clarice"));
        assertTrue(messageDiane.contains("Diane"));
        assertTrue(messageDiane.contains("turn"));
    }

    // TODO: Test more game stuff.
    // TODO: Test game end.
}
