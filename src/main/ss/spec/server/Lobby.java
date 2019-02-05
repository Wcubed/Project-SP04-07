package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.RandomTileBag;
import ss.spec.gamepieces.TileBag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

public class Lobby implements Runnable {

    // This object is just there to synchronize on.
    private final Object newClientSyncObject = new Object();
    private volatile boolean hasNewClient;
    private ClientPeer newClient;

    /**
     * To signal the lobby thread to stop running.
     */
    private volatile boolean stopLobbyThread;

    /**
     * These are clients that might not have a name yet.
     * And they have not yet requested a game.
     */
    private final ArrayList<ClientPeer> waitingClients;

    /**
     * Clients waiting for a game with a specific amount of players.
     */
    private final ArrayList<ClientPeer> waitingTwoPlayerGame;
    private final ArrayList<ClientPeer> waitingThreePlayerGame;
    private final ArrayList<ClientPeer> waitingFourPlayerGame;

    /**
     * Running games.
     */
    private final ArrayList<Game> games;


    /**
     * List of names that are already in use on this server.
     * Duplicate names are not allowed.
     */
    private final HashSet<String> usedNames;


    public Lobby() {
        hasNewClient = false;
        newClient = null;
        stopLobbyThread = false;

        waitingClients = new ArrayList<>();

        waitingTwoPlayerGame = new ArrayList<>();
        waitingThreePlayerGame = new ArrayList<>();
        waitingFourPlayerGame = new ArrayList<>();

        games = new ArrayList<>();

        usedNames = new HashSet<>();
    }

    /**
     * Call to stop the lobby thread.
     * The thread will stop on the next iteration.
     */
    public void stopLobbyThread() {
        stopLobbyThread = true;
    }

    /**
     * @return The total number of waiting clients.
     */
    public int getNumberOfWaitingClients() {
        return waitingClients.size() + waitingTwoPlayerGame.size() +
                waitingThreePlayerGame.size() + waitingFourPlayerGame.size();
    }

    public List<Game> getRunningGames() {
        return games;
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

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void doSingleLobbyIteration() {
        checkForNewClient();

        updateWaitingClients();

        updateWaitingForGameClients(waitingTwoPlayerGame, 2);
        updateWaitingForGameClients(waitingThreePlayerGame, 3);
        updateWaitingForGameClients(waitingFourPlayerGame, 4);

        checkOnRunningGames();
    }

    private void updateWaitingClients() {
        ListIterator<ClientPeer> clientIter = waitingClients.listIterator();

        // Check up on all the waiting clients.
        while (clientIter.hasNext()) {
            ClientPeer client = clientIter.next();

            if (client.isPeerConnected()) {
                switch (client.getState()) {
                    case LOBBY_VERIFY_NAME:
                        verifyClientName(client);
                        break;
                    case LOBBY_START_WAITING_FOR_PLAYERS:
                        putClientInChosenWaitingList(client, clientIter);
                        break;
                }
            } else {
                // Connection lost, client will be removed from list.
                System.out.println("Connection to client \'" + client.getName() + "\' lost.");

                // Remove the clients name from the list of used names.
                freeUpClientName(client);

                // Remove client from list.
                clientIter.remove();
            }
        }

        distributeChatMessages(waitingClients);
    }

    /**
     * @param clients         The list of waiting clients.
     * @param numberOfPlayers The number of players they are waiting for before starting the game.
     *                        Should be in the range [2-4].
     */
    private void updateWaitingForGameClients(List<ClientPeer> clients, int numberOfPlayers) {
        ListIterator<ClientPeer> clientIter = clients.listIterator();

        // Check up on all the waiting clients.
        while (clientIter.hasNext()) {
            ClientPeer client = clientIter.next();

            if (!client.isPeerConnected()) {
                // Connection lost, client will be removed from list.
                System.out.println("Connection to client \'" + client.getName() + "\' lost.");

                // Remove the clients name from the list of used names.
                freeUpClientName(client);

                // Remove client from list.
                clientIter.remove();
            }
        }

        distributeChatMessages(clients);

        if (clients.size() >= numberOfPlayers) {
            ArrayList<ClientPeer> players = new ArrayList<>();

            // Get the amount of players specified.
            for (int i = 0; i < numberOfPlayers; i++) {
                players.add(clients.remove(0));
            }

            startNewGame(players);
        }
    }

    private void checkOnRunningGames() {
        ListIterator<Game> gameIter = games.listIterator();

        while (gameIter.hasNext()) {
            Game game = gameIter.next();

            if (game.isGameOver()) {
                // If the game is flagged as over,
                // this means the game thread has already stopped.
                // Because that thread is the only one who can flag the game as over.
                // We can therefore clean up safely.
                for (Player player : game.getPlayers()) {
                    if (player.isPeerConnected()) {

                        player.getPeer().returningToLobby();
                        waitingClients.add(player.getPeer());

                    } else {
                        // Client has disconnected.
                        // Free up their name.
                        freeUpClientName(player.getPeer());
                    }
                }

                gameIter.remove();
            }
        }
    }

    /**
     * Puts the client in the waiting list for their chosen amount of players.
     * Removes the client from the general waiting list.
     *
     * @param client     The client in question.
     * @param clientIter The iterator to use to remove the client.
     */
    private void putClientInChosenWaitingList(ClientPeer client,
                                              ListIterator<ClientPeer> clientIter) {

        switch (client.getRequestedPlayerAmount()) {
            case 2:
                waitingTwoPlayerGame.add(client);
                client.signalWaitingForPlayers(getNamesFromClients(waitingTwoPlayerGame));
                clientIter.remove();
                break;
            case 3:
                waitingThreePlayerGame.add(client);
                client.signalWaitingForPlayers(getNamesFromClients(waitingThreePlayerGame));
                clientIter.remove();
                break;
            case 4:
                waitingFourPlayerGame.add(client);
                client.signalWaitingForPlayers(getNamesFromClients(waitingFourPlayerGame));
                clientIter.remove();
                break;
            default:
                // This should not actually be able to happen.
                System.out.println("Client " +
                        client.getName() +
                        " managed to request a weird amount of players: " +
                        client.getRequestedPlayerAmount());
        }
    }

    private void startNewGame(List<ClientPeer> players) {
        Board board = new Board();
        TileBag bag = new RandomTileBag();

        Game game = new Game(players, board, bag);

        Thread gameThread = new Thread(game);
        gameThread.start();

        games.add(game);
    }

    /**
     * Verifies whether a client's chosen name is valid.
     * And notifies the client of the decision.
     *
     * @param client The client to verify the name of.
     */
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

    /**
     * Call after a client has disconnected, make it's name available again.
     */
    private void freeUpClientName(ClientPeer client) {
        String name = client.getName();

        if (name != null) {
            usedNames.remove(name);
        }
    }

    /**
     * Get a list of names from a list of clients.
     *
     * @return The list of names.
     */
    private List<String> getNamesFromClients(List<ClientPeer> clients) {
        List<String> names = new ArrayList<>();

        for (ClientPeer client : clients) {
            names.add(client.getName());
        }

        return names;
    }

    private void distributeChatMessages(List<ClientPeer> clients) {
        for (ClientPeer client : clients) {
            String message = client.getNextChatMessage();

            while (message != null) {
                for (ClientPeer sendClient : clients) {
                    sendClient.sendChatMessage(client.getName(), message);
                }

                message = client.getNextChatMessage();
            }
        }
    }
}
