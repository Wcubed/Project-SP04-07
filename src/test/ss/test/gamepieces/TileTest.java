package ss.test.gamepieces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Tile;
import ss.spec.networking.DecodeException;

import static org.junit.jupiter.api.Assertions.*;

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
        Tile rotTile = tile1.rotate120();

        assertEquals(Color.PURPLE, rotTile.getFlatSide());
        assertEquals(Color.BLUE, rotTile.getClockwise1());
        assertEquals(Color.RED, rotTile.getClockwise2());
        assertEquals(2, rotTile.getPoints());

        // The original tile should be unchanged.
        assertEquals(Color.BLUE, tile1.getFlatSide());
        assertEquals(Color.RED, tile1.getClockwise1());
        assertEquals(Color.PURPLE, tile1.getClockwise2());
        assertEquals(2, tile1.getPoints());
    }

    @Test
    void rotate240() {
        Tile rotTile = tile1.rotate240();

        assertEquals(Color.RED, rotTile.getFlatSide());
        assertEquals(Color.PURPLE, rotTile.getClockwise1());
        assertEquals(Color.BLUE, rotTile.getClockwise2());
        assertEquals(2, rotTile.getPoints());

        // The original tile should be unchanged.
        assertEquals(Color.BLUE, tile1.getFlatSide());
        assertEquals(Color.RED, tile1.getClockwise1());
        assertEquals(Color.PURPLE, tile1.getClockwise2());
        assertEquals(2, tile1.getPoints());

        // Rotate the other one.
        Tile rotTile2 = tile2.rotate120();

        assertEquals(Color.GREEN, rotTile2.getFlatSide());
        assertEquals(Color.PURPLE, rotTile2.getClockwise1());
        assertEquals(Color.GREEN, rotTile2.getClockwise2());
        assertEquals(6, rotTile2.getPoints());
    }

    @Test
    void testEqualSides() {
        Tile rotTile = tile1.rotate120();

        assertEquals(rotTile.getFlatSide(), tile2.getFlatSide());
        assertNotEquals(rotTile.getClockwise1(), tile2.getClockwise1());
        assertNotEquals(rotTile.getClockwise2(), tile2.getClockwise2());
    }

    @Test
    void equality() {
        assertEquals(tile1, tile1);
        assertEquals(tile2, tile2);

        assertEquals(tile1, new Tile(Color.BLUE, Color.RED, Color.PURPLE, 2));

        assertNotEquals(tile1, tile2);
        assertNotEquals(tile2, tile1);

        assertNotEquals(tile1, tile1.rotate120());
        assertNotEquals(tile1, tile1.rotate240());

        assertNotEquals(tile1, null);
        assertNotEquals(tile1, 2);
        assertNotEquals(tile1.rotate120(), Color.GREEN);
    }

    @Test
    void equivalency() {
        assertTrue(tile1.isEquivalent(tile1));
        assertFalse(tile1.isEquivalent(tile2));
        assertFalse(tile2.isEquivalent(tile1));
        assertTrue(tile2.isEquivalent(tile2));

        assertTrue(tile1.isEquivalent(tile1.rotate120()));
        assertTrue(tile1.isEquivalent(tile1.rotate240()));
    }

    @Test
    void decode() throws DecodeException {
        assertEquals(new Tile(Color.RED, Color.PURPLE, Color.WHITE, 3),
                Tile.decode("RPW3"));

        assertEquals(new Tile(Color.PURPLE, Color.PURPLE, Color.PURPLE, 8),
                Tile.decode("PPP8"));

        assertThrows(DecodeException.class, () -> Tile.decode(""));
        assertThrows(DecodeException.class, () -> Tile.decode(null));
        assertThrows(DecodeException.class, () -> Tile.decode("    "));
        assertThrows(DecodeException.class, () -> Tile.decode("5DDD"));
        assertThrows(DecodeException.class, () -> Tile.decode("RPW+"));
        assertThrows(DecodeException.class, () -> Tile.decode("SDJFKsdfs"));
        assertThrows(DecodeException.class, () -> Tile.decode("R G B 4"));
        assertThrows(DecodeException.class, () -> Tile.decode("QRS8"));
    }
}