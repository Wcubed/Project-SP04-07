package ss.spec.server;

import ss.spec.gamepieces.Tile;

import java.util.ArrayList;

public class Player {

    public static int MAX_HAND_SIZE = 4;

    private ClientPeer peer;

    private int score;
    private ArrayList<Tile> tiles;

    public Player(ClientPeer peer) {
        this.peer = peer;

        score = 0;
        tiles = new ArrayList<>();
    }

    // ---------------------------------------------------------------------------------------------

    public ClientPeer getPeer() {
        return peer;
    }

    public int getScore() {
        return score;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    // ---------------------------------------------------------------------------------------------

    public String getName() {
        return peer.getName();
    }

    public boolean isPeerConnected() {
        return peer.isPeerConnected();
    }

    // ---------------------------------------------------------------------------------------------

    public void addTileToHand(Tile tile) {
        tiles.add(tile);
    }

    /**
     * Subtracts all the remaining tiles from the player's score.
     */
    public void endGameSubtractTilesFromScore() {
        for (Tile tile : tiles) {
            score -= tile.getPoints();
        }
    }
}
