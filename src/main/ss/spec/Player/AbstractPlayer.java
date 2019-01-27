package ss.spec.Player;

import ss.spec.gamepieces.Tile;

import java.util.ArrayList;

public abstract class AbstractPlayer {

    public static final int MAX_HAND_SIZE = 4;

    private int score;
    private ArrayList<Tile> tiles;


    public AbstractPlayer() {
        score = 0;
        tiles = new ArrayList<>();
    }


    public int getScore() {
        return score;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }


    public void addPoints(int points) {
        score += points;
    }

    /**
     * Will remove the given tile from the player's hand.
     * Won't fail if the player does not have the tile in the first place.
     *
     * @param tile The tile to remove.
     */
    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }

    public void addTileToHand(Tile tile) {
        tiles.add(tile);
    }

    /**
     * Check whether this player has the given tile in hand.
     *
     * @param tile The tile to check.
     * @return true when the player has the tile, false otherwise.
     */
    public boolean hasTileInHand(Tile tile) {
        boolean result = false;

        for (Tile checkTile : tiles) {
            if (checkTile.isEquivalent(tile)) {
                result = true;
                break;
            }
        }

        return result;
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
