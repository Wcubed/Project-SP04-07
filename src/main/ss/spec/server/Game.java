package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.TileBag;
import ss.spec.networking.ClientPeer;

import java.util.ArrayList;
import java.util.List;


public class Game implements Runnable {

    private boolean gameOver;

    private ArrayList<ClientPeer> players;
    private Board board;
    private TileBag bag;

    private ArrayList<String> turnOrder;

    /**
     * Instantiates the Game class with the given players, board and TileBag.
     *
     * @param players The participating players, order doesn't matter.
     * @param board   The board to play on.
     * @param bag     The tile bag to use. Will call `addAllStartingTiles` on this.
     */
    public Game(List<ClientPeer> players, Board board, TileBag bag) {
        this.players = new ArrayList<>(players);
        this.board = board;
        this.bag = bag;

        this.bag.addAllStartingTiles();

        gameOver = false;

        this.turnOrder = new ArrayList<>();
        // TODO: Decide turn order of players.
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
        while (!isGameOver()) {
            doSingleGameIteration();

            // TODO: Tweak sleeping for final application, maybe even remove it.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: cleanup?
    }


    public void doSingleGameIteration() {
        for (ClientPeer player : players) {
            if (!player.isPeerConnected()) {
                stopGamePlayerDisconnected(player.getName());
                break;
            }

            // TODO: Do actual game stuff.
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
