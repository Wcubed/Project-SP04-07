package ss.test.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.server.ClientPeer;
import ss.spec.server.Game;
import ss.spec.server.Lobby;
import ss.spec.server.Player;
import ss.test.networking.MockConnection;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

    private Lobby lobby;

    @BeforeEach
    void setUp() {
        lobby = new Lobby();
    }

    @Test
    void initialStatus() {
        assertEquals(0, lobby.getNumberOfWaitingClients());
    }

    @Test
    void addClients() {
        MockConnection connection = new MockConnection();
        ClientPeer newClient = new ClientPeer(connection);

        lobby.addNewClient(newClient);
        lobby.doSingleLobbyIteration();

        assertEquals(1, lobby.getNumberOfWaitingClients());

        lobby.addNewClient(newClient);
        lobby.doSingleLobbyIteration();

        assertEquals(2, lobby.getNumberOfWaitingClients());

        lobby.addNewClient(newClient);
        lobby.doSingleLobbyIteration();

        assertEquals(3, lobby.getNumberOfWaitingClients());
    }

    @Test
    void clientsDisconnect() {
        MockConnection connection = new MockConnection();
        ClientPeer newClient = new ClientPeer(connection);

        lobby.addNewClient(newClient);
        lobby.doSingleLobbyIteration();

        assertEquals(1, lobby.getNumberOfWaitingClients());

        // Kill the connection.
        connection.killConnection();
        // Make the client realize it's disconnected.
        newClient.sendMessage("bla");

        lobby.doSingleLobbyIteration();

        // The lobby should have removed the client.
        assertEquals(0, lobby.getNumberOfWaitingClients());
    }

    @Test
    void clientNamesTest() {
        MockConnection connection1 = new MockConnection();
        ClientPeer client1 = new ClientPeer(connection1);

        MockConnection connection2 = new MockConnection();
        ClientPeer client2 = new ClientPeer(connection2);

        lobby.addNewClient(client1);
        lobby.doSingleLobbyIteration();
        lobby.addNewClient(client2);
        lobby.doSingleLobbyIteration();

        client1.handleReceivedMessage("connect Bob");

        lobby.doSingleLobbyIteration();
        // Multiple iterations should not have any effect.
        lobby.doSingleLobbyIteration();
        lobby.doSingleLobbyIteration();
        lobby.doSingleLobbyIteration();

        assertEquals(ClientPeer.State.PEER_AWAITING_GAME_REQUEST, client1.getState());

        // Name already taken.
        client2.handleReceivedMessage("connect Bob");

        lobby.doSingleLobbyIteration();

        // Client2 should be back on the connect message.
        assertEquals(ClientPeer.State.PEER_AWAITING_CONNECT_MESSAGE, client2.getState());

        // Name is unique.
        client2.handleReceivedMessage("connect Peter");
        lobby.doSingleLobbyIteration();
        assertEquals(ClientPeer.State.PEER_AWAITING_GAME_REQUEST, client2.getState());

        // Killing client1
        connection1.killConnection();
        client1.sendMessage("Bla");
        lobby.doSingleLobbyIteration();
        // Client1 is now disconnected.

        // Bob should now be a valid name again.
        MockConnection connection3 = new MockConnection();
        ClientPeer client3 = new ClientPeer(connection3);

        client3.handleReceivedMessage("connect Bob");

        lobby.addNewClient(client3);
        lobby.doSingleLobbyIteration();

        assertEquals("Bob", client3.getName());
        assertEquals(ClientPeer.State.PEER_AWAITING_GAME_REQUEST, client3.getState());
    }

    @Test
    void waitingForPlayersTest() {
        MockConnection connection1 = new MockConnection();
        ClientPeer client1 = new ClientPeer(connection1);
        MockConnection connection2 = new MockConnection();
        ClientPeer client2 = new ClientPeer(connection2);
        MockConnection connection3 = new MockConnection();
        ClientPeer client3 = new ClientPeer(connection3);

        lobby.addNewClient(client1);
        lobby.doSingleLobbyIteration();
        lobby.addNewClient(client2);
        lobby.doSingleLobbyIteration();
        lobby.addNewClient(client3);
        lobby.doSingleLobbyIteration();

        client1.handleReceivedMessage("connect Bob");
        client2.handleReceivedMessage("connect John");
        client3.handleReceivedMessage("connect C3-P0");
        lobby.doSingleLobbyIteration();
        // Purge the welcome messages.
        connection1.purgeSentMessages();
        connection2.purgeSentMessages();
        connection3.purgeSentMessages();

        // Request a 4 player game.
        client1.handleReceivedMessage("request 4");
        lobby.doSingleLobbyIteration();

        assertEquals(ClientPeer.State.LOBBY_WAITING_FOR_PLAYERS, client1.getState());
        assertEquals("waiting Bob", connection1.readSentMessage());

        // Second client requests a 4 player game.
        client2.handleReceivedMessage("request 4");
        lobby.doSingleLobbyIteration();

        // Client 2 should get a waiting message with client1's name in it.
        assertEquals(ClientPeer.State.LOBBY_WAITING_FOR_PLAYERS, client2.getState());
        assertTrue(connection2.readSentMessage().contains("Bob"));


        // Client 3 requests a 3 player game.
        client3.handleReceivedMessage("request 3");
        lobby.doSingleLobbyIteration();

        // Client 3 should only have his own name in the waiting list..
        assertEquals("waiting C3-P0", connection3.readSentMessage());
    }

    @Test
    void waitingForPlayersDisconnect() {
        MockConnection connection1 = new MockConnection();
        ClientPeer client1 = new ClientPeer(connection1);

        client1.handleReceivedMessage("connect C3-P0");

        lobby.addNewClient(client1);
        lobby.doSingleLobbyIteration();

        client1.handleReceivedMessage("request 2");
        lobby.doSingleLobbyIteration();

        assertEquals(1, lobby.getNumberOfWaitingClients());

        // Disconnect the client.
        connection1.killConnection();
        client1.sendMessage("Irrelevant");

        lobby.doSingleLobbyIteration();

        // Now there should be no one.
        assertEquals(0, lobby.getNumberOfWaitingClients());
    }

    /**
     * This would probably be better as 2 or 3 separate tests,
     * if this was a program that was going to be used in actual production.
     * However, for now it is sufficient to make sure stuff works.
     */
    @Test
    void startAndStopGameTest() {
        MockConnection connection1 = new MockConnection();
        ClientPeer client1 = new ClientPeer(connection1);
        MockConnection connection2 = new MockConnection();
        ClientPeer client2 = new ClientPeer(connection2);

        client1.handleReceivedMessage("connect Bob");
        client2.handleReceivedMessage("connect John");

        lobby.addNewClient(client1);
        lobby.doSingleLobbyIteration();
        lobby.addNewClient(client2);
        lobby.doSingleLobbyIteration();

        // Purge the messages that we don't care about.
        connection1.purgeSentMessages();
        connection2.purgeSentMessages();

        client1.handleReceivedMessage("request 2");
        client2.handleReceivedMessage("request 2");

        lobby.doSingleLobbyIteration();

        // A game should be running now.
        ArrayList<Game> games = new ArrayList<>(lobby.getRunningGames());
        assertEquals(1, games.size());
        assertFalse(games.get(0).isGameOver());

        // We expect 2 players.
        ArrayList<Player> players = new ArrayList<>(games.get(0).getPlayers());
        assertEquals(2, players.size());

        // And there should be no-one left in the lobby.
        assertEquals(0, lobby.getNumberOfWaitingClients());

        // Disconnect a client.
        // We want to test both the "client still alive after game stops" and
        // "client disconnected after game stops" cases of the lobby game cleanup.
        connection1.killConnection();
        // Make sure the client realizes it's dead.
        client1.sendMessage("Irrelevant");


        // Let's not wait for the game thread to stop, and kill the game on our own.
        games.get(0).gameIsNowOver();

        // Let the lobby do the game cleanup.
        lobby.doSingleLobbyIteration();

        // There should be 1 client left in the lobby.
        assertEquals(1, lobby.getNumberOfWaitingClients());
        // And no running games.
        assertEquals(0, lobby.getRunningGames().size());

        // Connecting as "Bob" should be ok again, as he has just disconnected.
        MockConnection connection3 = new MockConnection();
        ClientPeer client3 = new ClientPeer(connection3);
        client3.handleReceivedMessage("connect Bob");

        lobby.addNewClient(client3);
        lobby.doSingleLobbyIteration();

        assertEquals("welcome", connection3.readSentMessage());
    }
}
