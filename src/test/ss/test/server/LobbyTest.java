package ss.test.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.networking.ClientPeer;
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
}
