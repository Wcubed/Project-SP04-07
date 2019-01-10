package ss.spec.board;

import ss.spec.Tile;

public class BoardSpace {

    private int id;
    private int scoreMultiplier;
    private Tile tile;

    public BoardSpace(int id, int scoreMulitpier) {
        this.id = id;
        this.scoreMultiplier = scoreMulitpier;
    }

    public int getScoreMultiplier() {
        return scoreMultiplier;
    }

    public int getId() {
        return id;
    }

    public Tile getTile() {
        return tile;
    }

    public boolean isBonusSpace() {
        return scoreMultiplier > 1;
    }

    public void placeTile(Tile newTile) {
        tile = newTile;
    }

    public boolean hasTile() {
        return tile != null;
    }
}
