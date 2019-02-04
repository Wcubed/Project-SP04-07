package ss.test.gamepieces;

import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.BoardCoordinates;
import ss.spec.gamepieces.IndexException;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void getFlatNeighbourCoordinates() {
        assertEquals(new BoardCoordinates(0, 0),
                new BoardCoordinates(1, 0).getFlatNeighbourCoordinates());
        assertEquals(new BoardCoordinates(3, 2),
                new BoardCoordinates(2, 2).getFlatNeighbourCoordinates());
        assertEquals(new BoardCoordinates(5, -2),
                new BoardCoordinates(4, -2).getFlatNeighbourCoordinates());


        assertNull(new BoardCoordinates(5, -5).getFlatNeighbourCoordinates());
    }

    @Test
    void getClockwiseNeighbourCoordinates() {
        assertEquals(new BoardCoordinates(1, 1),
                new BoardCoordinates(1, 0).getClockwiseNeighbourCoordinates());
        assertEquals(new BoardCoordinates(2, 1),
                new BoardCoordinates(2, 2).getClockwiseNeighbourCoordinates());
        assertEquals(new BoardCoordinates(4, -3),
                new BoardCoordinates(4, -2).getClockwiseNeighbourCoordinates());


        assertNull(new BoardCoordinates(4, -4).getClockwiseNeighbourCoordinates());
    }

    @Test
    void getCounterclockwiseNeighbourCoordinates() {
        assertEquals(new BoardCoordinates(1, -1),
                new BoardCoordinates(1, 0).getCounterclockwiseNeighbourCoordinates());
        assertEquals(new BoardCoordinates(2, -1),
                new BoardCoordinates(2, -2).getCounterclockwiseNeighbourCoordinates());
        assertEquals(new BoardCoordinates(4, -1),
                new BoardCoordinates(4, -2).getCounterclockwiseNeighbourCoordinates());


        assertNull(new BoardCoordinates(2, 2).getCounterclockwiseNeighbourCoordinates());
    }
}
