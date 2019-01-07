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
        // Three same sides.
        int points = 6;
        tiles.add(new Tile(Color.RED, Color.RED, Color.RED, points));
        tiles.add(new Tile(Color.BLUE, Color.BLUE, Color.BLUE, points));
        tiles.add(new Tile(Color.GREEN, Color.GREEN, Color.GREEN, points));
        tiles.add(new Tile(Color.YELLOW, Color.YELLOW, Color.YELLOW, points));
        tiles.add(new Tile(Color.PURPLE, Color.PURPLE, Color.PURPLE, points));

        // Two of the same side.
        points = 5;
        tiles.add(new Tile(Color.RED, Color.RED, Color.YELLOW, points));
        tiles.add(new Tile(Color.RED, Color.RED, Color.PURPLE, points));
        tiles.add(new Tile(Color.BLUE, Color.BLUE, Color.RED, points));
        tiles.add(new Tile(Color.BLUE, Color.BLUE, Color.PURPLE, points));
        tiles.add(new Tile(Color.GREEN, Color.GREEN, Color.RED, points));
        tiles.add(new Tile(Color.GREEN, Color.GREEN, Color.BLUE, points));
        tiles.add(new Tile(Color.YELLOW, Color.YELLOW, Color.GREEN, points));
        tiles.add(new Tile(Color.YELLOW, Color.YELLOW, Color.BLUE, points));
        tiles.add(new Tile(Color.PURPLE, Color.PURPLE, Color.YELLOW, points));
        tiles.add(new Tile(Color.PURPLE, Color.PURPLE, Color.GREEN, points));

        points = 4;
        tiles.add(new Tile(Color.RED, Color.RED, Color.BLUE, points));
        tiles.add(new Tile(Color.RED, Color.RED, Color.GREEN, points));
        tiles.add(new Tile(Color.BLUE, Color.BLUE, Color.GREEN, points));
        tiles.add(new Tile(Color.BLUE, Color.BLUE, Color.YELLOW, points));
        tiles.add(new Tile(Color.GREEN, Color.GREEN, Color.YELLOW, points));
        tiles.add(new Tile(Color.GREEN, Color.GREEN, Color.PURPLE, points));
        tiles.add(new Tile(Color.YELLOW, Color.YELLOW, Color.RED, points));
        tiles.add(new Tile(Color.YELLOW, Color.YELLOW, Color.PURPLE, points));
        tiles.add(new Tile(Color.PURPLE, Color.PURPLE, Color.RED, points));
        tiles.add(new Tile(Color.PURPLE, Color.PURPLE, Color.BLUE, points));

        // All sides different colors.
        points = 3;
        tiles.add(new Tile(Color.YELLOW, Color.BLUE, Color.PURPLE, points));
        tiles.add(new Tile(Color.RED, Color.GREEN, Color.YELLOW, points));
        tiles.add(new Tile(Color.BLUE, Color.GREEN, Color.PURPLE, points));
        tiles.add(new Tile(Color.GREEN, Color.RED, Color.BLUE, points));

        points = 2;
        tiles.add(new Tile(Color.BLUE, Color.RED, Color.PURPLE, points));
        tiles.add(new Tile(Color.YELLOW, Color.PURPLE, Color.RED, points));
        tiles.add(new Tile(Color.YELLOW, Color.PURPLE, Color.GREEN, points));

        points = 1;
        tiles.add(new Tile(Color.GREEN, Color.RED, Color.PURPLE, points));
        tiles.add(new Tile(Color.BLUE, Color.YELLOW, Color.GREEN, points));
        tiles.add(new Tile(Color.RED, Color.YELLOW, Color.BLUE, points));

        // Joker.
        points = 1;
        tiles.add(new Tile(Color.WHITE, Color.WHITE, Color.WHITE, points));
    }
}
