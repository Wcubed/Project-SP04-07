package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.TileBag;
import ss.spec.networking.ClientPeer;

import java.util.ArrayList;


public class Game implements Runnable {

    private boolean gameOver;

    /**
     * The list of players in turn order.
     */
    private ArrayList<ClientPeer> players;
    private Board board;
    private TileBag bag;

    /**
     * Instantiates the Game class with the given players, board and TileBag.
     *
     * @param players The participating players, order doesn't matter.
     * @param board   The board to play on.
     * @param bag     The tile bag to use. Will call `addAllStartingTiles` on this.
     */
    public Game(ArrayList<ClientPeer> players, Board board, TileBag bag) {
        this.board = board;
        this.bag = bag;
        this.bag.addAllStartingTiles();

        gameOver = false;

        // TODO: Decide starting order of players.
        this.players = players;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * @return The list of players in turn order.
     */
    public ArrayList<ClientPeer> getOrderedPlayerList() {
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
        // TODO: implement this.
    }
}
