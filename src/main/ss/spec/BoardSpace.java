package ss.spec;

public class BoardSpace {

    private int scoreMultiplier;
    private Tile tile;

    public BoardSpace(int scoreMulitpier) {
        this.scoreMultiplier = scoreMulitpier;
    }

    public int getScoreMultiplier() {
        return scoreMultiplier;
    }

    public Tile getTile() {
        return tile;
    }

    public void placeTile(Tile newTile) {
        tile = newTile;
    }

    public boolean hasTile() {
        return tile != null;
    }
}
