package ss.test;

import org.junit.jupiter.api.Test;
import ss.spec.BoardSpace;
import ss.spec.Color;
import ss.spec.Tile;

import static org.junit.jupiter.api.Assertions.*;

class BoardSpaceTest {

    @Test
    void scoreMultiplier() {
        BoardSpace space1 = new BoardSpace(1);
        BoardSpace space4 = new BoardSpace(4);

        assertEquals(1, space1.getScoreMultiplier());
        assertEquals(4, space4.getScoreMultiplier());
    }

    @Test
    void placeTile() {
        BoardSpace space = new BoardSpace(1);
        Tile tile = new Tile(Color.PURPLE, Color.RED, Color.BLUE, 5);

        assertFalse(space.hasTile());
        assertNull(space.getTile());

        space.placeTile(tile);

        assertTrue(space.hasTile());
        assertEquals(tile, space.getTile());
    }
}
