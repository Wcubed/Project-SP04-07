package ss.spec.gamepieces;

public class Move {

    private Tile tile;
    private int index;

    public Move(Tile tile, int index) {
        this.tile = tile;
        this.index = index;
    }

    public Tile getTile() {
        return tile;
    }

    public int getIndex() {
        return index;
    }
}
