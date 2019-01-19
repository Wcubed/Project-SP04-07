package ss.test.gamepieces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Tile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TileTest {

    private Tile tile1;
    private Tile tile2;

    @BeforeEach
    void setUp() {
        tile1 = new Tile(Color.BLUE, Color.RED, Color.PURPLE, 2);
        tile2 = new Tile(Color.PURPLE, Color.GREEN, Color.GREEN, 6);
    }

    @Test
    void startingState() {
        assertEquals(Color.BLUE, tile1.getFlatSide());
        assertEquals(Color.RED, tile1.getClockwise1());
        assertEquals(Color.PURPLE, tile1.getClockwise2());

        assertEquals(2, tile1.getPoints());

        assertEquals(Color.PURPLE, tile2.getFlatSide());
        assertEquals(Color.GREEN, tile2.getClockwise1());
        assertEquals(Color.GREEN, tile2.getClockwise2());

        assertEquals(6, tile2.getPoints());
    }

    @Test
    void rotate120() {
        tile1.rotate120();

        assertEquals(Color.PURPLE, tile1.getFlatSide());
        assertEquals(Color.BLUE, tile1.getClockwise1());
        assertEquals(Color.RED, tile1.getClockwise2());
        assertEquals(2, tile1.getPoints());
    }

    @Test
    void rotate240() {
        tile1.rotate240();

        assertEquals(Color.RED, tile1.getFlatSide());
        assertEquals(Color.PURPLE, tile1.getClockwise1());
        assertEquals(Color.BLUE, tile1.getClockwise2());
        assertEquals(2, tile1.getPoints());

        tile2.rotate120();

        assertEquals(Color.GREEN, tile2.getFlatSide());
        assertEquals(Color.PURPLE, tile2.getClockwise1());
        assertEquals(Color.GREEN, tile2.getClockwise2());
        assertEquals(6, tile2.getPoints());
    }

    @Test
    void testEqualSides() {
        tile1.rotate120();

        assertEquals(tile1.getFlatSide(), tile2.getFlatSide());
        assertNotEquals(tile1.getClockwise1(), tile2.getClockwise1());
        assertNotEquals(tile1.getClockwise2(), tile2.getClockwise2());
    }
}