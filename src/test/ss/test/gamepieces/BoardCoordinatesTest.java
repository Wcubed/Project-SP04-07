package ss.test.gamepieces;

import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.BoardCoordinates;
import ss.spec.gamepieces.IndexException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardCoordinatesTest {

    @Test
    void fromIndex() throws IndexException {
        assertEquals(new BoardCoordinates(2, -2), BoardCoordinates.fromIndex(4));
        assertEquals(new BoardCoordinates(4, 0), BoardCoordinates.fromIndex(20));
        assertEquals(new BoardCoordinates(5, 5), BoardCoordinates.fromIndex(35));

        // Out of bounds index's.
        assertThrows(IndexException.class, () -> BoardCoordinates.fromIndex(-10));
        assertThrows(IndexException.class, () -> BoardCoordinates.fromIndex(37));
    }

    @Test
    void asIndex() throws IndexException {
        assertEquals(1, new BoardCoordinates(1, -1).asIndex());
        assertEquals(35, new BoardCoordinates(5, 5).asIndex());
        assertEquals(6, new BoardCoordinates(2, 0).asIndex());

        // Out of bounds index's.
        assertThrows(IndexException.class,
                () -> new BoardCoordinates(-10, 12).asIndex());
        assertThrows(IndexException.class,
                () -> new BoardCoordinates(6, 0).asIndex());
    }
}
