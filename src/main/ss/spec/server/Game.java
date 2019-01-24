package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Tile;
import ss.spec.gamepieces.TileBag;
import ss.spec.networking.ClientPeer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Game implements Runnable {

    private boolean gameOver;

    private ArrayList<ClientPeer> players;
    private Board board;
    private TileBag bag;

    private ArrayList<String> turnOrder;
    private int currentTurnPlayer;

    private HashMap<String, ArrayList<Tile>> playerTiles;

    /**
     * Instantiates the Game class with the given players, board and TileBag.
     *
     * @param players The participating players, order doesn't matter.
     * @param board   The board to play on.
     * @param bag     The tile bag to use.
     */
    public Game(List<ClientPeer> players, Board board, TileBag bag) {
        this.players = new ArrayList<>(players);
        this.board = board;
        this.bag = bag;

        gameOver = false;

        this.turnOrder = new ArrayList<>();
        this.playerTiles = new HashMap<>();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void gameIsNowOver() {
        gameOver = true;
    }

    /**
     * @return The list of players.
     */
    public List<ClientPeer> getPlayers() {
        return players;
    }

    @Override
    public void run() {
        setUpGame();

        while (!isGameOver()) {
            doSingleGameThreadIteration();

            // TODO: Tweak sleeping for final application, maybe even remove it?
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: cleanup?
    }

    public void setUpGame() {
        bag.addAllStartingTiles();

        // Set up tile lists.
        for (ClientPeer player : players) {
            playerTiles.put(player.getName(), new ArrayList<>());
        }

        // TODO: Decide turn order.


        // Let the clients know the game is on.
        for (ClientPeer player : players) {
            player.sendStartMessage(turnOrder);
            player.sendOrderMessage(turnOrder);
        }

    }


    public void doSingleGameThreadIteration() {
        for (ClientPeer player : players) {
            if (!player.isPeerConnected()) {
                stopGamePlayerDisconnected(player.getName());
                break;
            }

            // TODO: Do actual game stuff.
        }
    }

    private void sendTileAnnouncements() {
        for (ClientPeer player : players) {
            player.sendTileAnnouncement(playerTiles);
        }
    }

    /**
     * Stops the game because a player disconnected.
     * Informs the players of this development.
     *
     * @param playerName The name of the player who disconnected.
     */
    private void stopGamePlayerDisconnected(String playerName) {
        for (ClientPeer player : players) {
            // We are also sending this message to the one who disconnected.
            // This is not a problem however, as that is handled gracefully.
            player.sendPlayerLeftMessage(playerName);
        }

        gameIsNowOver();
    }
}
