package ss.test.board;

import org.junit.jupiter.api.Test;
import ss.spec.Color;
import ss.spec.Tile;
import ss.spec.board.BoardSpace;

import static org.junit.jupiter.api.Assertions.*;

class BoardSpaceTest {

    @Test
    void initialValues() {
        BoardSpace space1 = new BoardSpace(1, 1);
        BoardSpace space4 = new BoardSpace(3, 4);

        assertEquals(1, space1.getId());
        assertEquals(3, space4.getId());

        assertEquals(1, space1.getScoreMultiplier());
        assertEquals(4, space4.getScoreMultiplier());
    }

    @Test
    void placeTile() {
        BoardSpace space = new BoardSpace(0, 1);
        Tile tile = new Tile(Color.PURPLE, Color.RED, Color.BLUE, 5);

        assertFalse(space.hasTile());
        assertNull(space.getTile());

        space.placeTile(tile);

        assertTrue(space.hasTile());
        assertEquals(tile, space.getTile());
    }

    @Test
    void isBonusSpace() {
        BoardSpace space1 = new BoardSpace(4, 1);
        BoardSpace space3 = new BoardSpace(7, 3);

        assertFalse(space1.isBonusSpace());
        assertTrue(space3.isBonusSpace());
    }
}
