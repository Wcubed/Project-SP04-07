package ss.test.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.networking.ClientPeer;
import ss.spec.networking.ClientState;
import ss.spec.server.Lobby;
import ss.test.networking.MockConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(ClientState.PEER_AWAITING_GAME_REQUEST, client1.getState());

        // Name already taken.
        client2.handleReceivedMessage("connect Bob");

        lobby.doSingleLobbyIteration();

        // Client2 should be back on the connect message.
        assertEquals(ClientState.PEER_AWAITING_CONNECT_MESSAGE, client2.getState());

        // Name is unique.
        client2.handleReceivedMessage("connect Peter");
        lobby.doSingleLobbyIteration();
        assertEquals(ClientState.PEER_AWAITING_GAME_REQUEST, client2.getState());

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
        assertEquals(ClientState.PEER_AWAITING_GAME_REQUEST, client3.getState());
    }
}
