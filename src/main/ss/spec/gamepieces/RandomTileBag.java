package ss.spec;

import java.util.ArrayList;
import java.util.Random;

public class TileBag {

    private ArrayList<Tile> tiles;
    private Random random;

    public TileBag() {
        tiles = new ArrayList<>();
        random = new Random();
    }

    public void addTile(Tile tile) {tiles.add(tile);
    }

    public int getSize() {
        return tiles.size();
    }

    /**
     * Removes a random tile from the bag and returns it.
     *
     * @return The tile that was removed from the bag.
     */
    public Tile takeRandomTile() {
        return tiles.remove(random.nextInt(getSize()));
    }

    public void addAllStartingTiles() {
        // Three same sides.
        int points = 6;
        addTile(new Tile(Color.RED, Color.RED, Color.RED, points));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.BLUE, points));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.GREEN, points));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.YELLOW, points));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.PURPLE, points));

        // Two of the same side.
        points = 5;
        addTile(new Tile(Color.RED, Color.RED, Color.YELLOW, points));
        addTile(new Tile(Color.RED, Color.RED, Color.PURPLE, points));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.RED, points));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.PURPLE, points));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.RED, points));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.BLUE, points));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.GREEN, points));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.BLUE, points));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.YELLOW, points));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.GREEN, points));

        points = 4;
        addTile(new Tile(Color.RED, Color.RED, Color.BLUE, points));
        addTile(new Tile(Color.RED, Color.RED, Color.GREEN, points));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.GREEN, points));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.YELLOW, points));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.YELLOW, points));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.PURPLE, points));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.RED, points));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.PURPLE, points));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.RED, points));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.BLUE, points));

        // All sides different colors.
        points = 3;
        addTile(new Tile(Color.YELLOW, Color.BLUE, Color.PURPLE, points));
        addTile(new Tile(Color.RED, Color.GREEN, Color.YELLOW, points));
        addTile(new Tile(Color.BLUE, Color.GREEN, Color.PURPLE, points));
        addTile(new Tile(Color.GREEN, Color.RED, Color.BLUE, points));

        points = 2;
        addTile(new Tile(Color.BLUE, Color.RED, Color.PURPLE, points));
        addTile(new Tile(Color.YELLOW, Color.PURPLE, Color.RED, points));
        addTile(new Tile(Color.YELLOW, Color.PURPLE, Color.GREEN, points));

        points = 1;
        addTile(new Tile(Color.GREEN, Color.RED, Color.PURPLE, points));
        addTile(new Tile(Color.BLUE, Color.YELLOW, Color.GREEN, points));
        addTile(new Tile(Color.RED, Color.YELLOW, Color.BLUE, points));

        // Joker.
        points = 1;
        addTile(new Tile(Color.WHITE, Color.WHITE, Color.WHITE, points));
    }
}
