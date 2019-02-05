package ss.spec.gamepieces;

public class BoardSpace {

    private final int id;
    private final int scoreMultiplier;
    private Tile tile;

    public BoardSpace(int id, int scoreMultiplier) {
        this.id = id;
        this.scoreMultiplier = scoreMultiplier;
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
