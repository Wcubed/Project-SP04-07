package ss.test.gamepieces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.EmptyTileBagException;
import ss.spec.gamepieces.RandomTileBag;
import ss.spec.gamepieces.Tile;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomTileBagTest {
    private RandomTileBag bag;

    @BeforeEach
    void setUp() {
        bag = new RandomTileBag();
    }

    @Test
    void startingState() {
        assertEquals(0, bag.getNumTilesLeft());
    }

    @Test
    void addTile() {
        bag.addTile(new Tile(Color.PURPLE, Color.GREEN, Color.RED, 7));
        assertEquals(1, bag.getNumTilesLeft());

        bag.addTile(new Tile(Color.RED, Color.GREEN, Color.RED, 2));
        assertEquals(2, bag.getNumTilesLeft());
    }

    @Test
    void addAllStartingTiles() {
        bag.addAllStartingTiles();
        // There are 36 starting tiles in a game of Spectrangle.
        assertEquals(36, bag.getNumTilesLeft());
    }

    @Test
    void takeRandomTile() throws EmptyTileBagException {
        Tile tile = new Tile(Color.PURPLE, Color.GREEN, Color.RED, 7);

        bag.addTile(tile);
        assertEquals(tile, bag.takeTile());
        assertEquals(0, bag.getNumTilesLeft());

        bag.addTile(tile);
        bag.addTile(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 2));

        bag.takeTile();
        assertEquals(1, bag.getNumTilesLeft());
    }
}
