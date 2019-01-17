package ss.spec.server;

import ss.spec.networking.ClientPeer;

import java.util.ArrayList;
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

    public Lobby() {
        hasNewClient = false;
        newClient = null;
        stopLobbyThread = false;

        waitingClients = new ArrayList<>();
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
                client.sendMessage("Hello World!");
            } else {

                // Connection lost, client will be removed from list.
                // TODO: Nice logging.
                System.out.println("Connection to client lost.");

                // Remove client from list.
                clientIter.remove();
            }
        }

        // TODO: Tweak sleeping for final application, maybe even remove it.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
