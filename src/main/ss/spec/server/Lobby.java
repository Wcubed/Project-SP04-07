package ss.spec.server;

import ss.spec.networking.ClientPeer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

public class Lobby implements Runnable {

    // This object is just there to synchronize on.
    // TODO: There might be a better way than using an object.
    private final Object newClientSyncObject = new Object();
    // TODO: Do we need volatile here?
    private volatile boolean hasNewClient;
    private ClientPeer newClient;

    /**
     * To signal the lobby thread to stop running.
     */
    private volatile boolean stopLobbyThread;

    private ArrayList<ClientPeer> waitingClients;

    /**
     * List of names that are already in use on this server.
     * Duplicate names are not allowed.
     */
    private HashSet<String> usedNames;


    public Lobby() {
        hasNewClient = false;
        newClient = null;
        stopLobbyThread = false;

        waitingClients = new ArrayList<>();
        usedNames = new HashSet<>();
    }

    /**
     * Call to stop the lobby thread.
     * The thread will stop on the next iteration.
     */
    public void stopLobbyThread() {
        stopLobbyThread = true;
    }

    public int getNumberOfWaitingClients() {
        return waitingClients.size();
    }

    /**
     * Called by the main thread to add newly incoming clients.
     *
     * @param client The new client to add to the lobby.
     */
    public void addNewClient(ClientPeer client) {
        synchronized (newClientSyncObject) {
            // Wait until the previous new client has been handled.
            while (hasNewClient) {
                try {
                    newClient.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // We can add the new client.
            newClient = client;
            hasNewClient = true;
        }
    }

    /**
     * Called by the lobby thread to handle the newly incoming clients.
     */
    private void checkForNewClient() {
        synchronized (newClientSyncObject) {
            if (hasNewClient) {
                // Add the new client to the waiting clients.
                waitingClients.add(newClient);

                newClient = null;
                hasNewClient = false;
                newClientSyncObject.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        while (!stopLobbyThread) {
            doSingleLobbyIteration();

            // TODO: Tweak sleeping for final application, maybe even remove it.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void doSingleLobbyIteration() {
        // TODO: Actually implement lobby code.
        // For now, simply send a message once in a while.

        checkForNewClient();

        ListIterator<ClientPeer> clientIter = waitingClients.listIterator();

        // Check up on all the clients.
        while (clientIter.hasNext()) {
            ClientPeer client = clientIter.next();

            if (client.isPeerConnected()) {
                switch (client.getState()) {
                    case LOBBY_VERIFY_NAME:
                        verifyClientName(client);
                        break;
                    case LOBBY_START_WAITING_FOR_PLAYERS:

                        break;
                    case LOBBY_AWAITING_GAME_START:

                        break;
                }
            } else {

                // Connection lost, client will be removed from list.
                // TODO: Nice logging.
                System.out.println("Connection to client lost.");

                // Remove the clients name from the list of used names.
                String name = client.getName();

                if (name != null) {
                    usedNames.remove(name);
                }

                // Remove client from list.
                clientIter.remove();
            }
        }
    }

    private void verifyClientName(ClientPeer client) {
        String name = client.getName();

        if (name == null || usedNames.contains(name) || name.contains(" ")) {
            // No name, bad name or already used name.
            client.rejectName();
        } else {
            client.acceptName();
            usedNames.add(name);
        }
    }
}
