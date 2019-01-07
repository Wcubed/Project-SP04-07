package ss.spec;

import java.util.ArrayList;

public class TileBag {

    private ArrayList<Tile> tiles;

    public TileBag() {
        tiles = new ArrayList<>();
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public int getSize() {
        return tiles.size();
    }

    public void addAllStartingTiles() {
        tiles.add(new Tile(Color.RED, Color.RED, Color.RED, 6));
        tiles.add(new Tile(Color.GREEN, Color.GREEN, Color.GREEN, 6));
        tiles.add(new Tile(Color.YELLOW, Color.YELLOW, Color.YELLOW, 6));
        tiles.add(new Tile(Color.PURPLE, Color.PURPLE, Color.PURPLE, 6));
    }
}
