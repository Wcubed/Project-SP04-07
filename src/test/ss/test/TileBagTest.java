package ss.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.Color;
import ss.spec.Tile;
import ss.spec.TileBag;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TileBagTest {
    private TileBag bag;

    @BeforeEach
    void setUp() {
        bag = new TileBag();
    }

    @Test
    void startingState() {
        assertEquals(0, bag.getSize());
    }

    @Test
    void addTile() {
        bag.addTile(new Tile(Color.PURPLE, Color.GREEN, Color.RED, 7));
        assertEquals(1, bag.getSize());

        bag.addTile(new Tile(Color.RED, Color.GREEN, Color.RED, 2));
        assertEquals(2, bag.getSize());
    }

}
