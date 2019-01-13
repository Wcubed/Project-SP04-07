package ss.test.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.Color;
import ss.spec.InvalidMoveException;
import ss.spec.Tile;
import ss.spec.board.Board;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void bonusSpaces() {
        assertEquals(2, board.getSpace(10).getScoreMultiplier());
        assertEquals(2, board.getSpace(14).getScoreMultiplier());
        assertEquals(2, board.getSpace(30).getScoreMultiplier());

        assertEquals(3, board.getSpace(2).getScoreMultiplier());
        assertEquals(3, board.getSpace(26).getScoreMultiplier());
        assertEquals(3, board.getSpace(34).getScoreMultiplier());

        assertEquals(4, board.getSpace(11).getScoreMultiplier());
        assertEquals(4, board.getSpace(13).getScoreMultiplier());
        assertEquals(4, board.getSpace(20).getScoreMultiplier());


        assertEquals(1, board.getSpace(0).getScoreMultiplier());
        assertEquals(1, board.getSpace(33).getScoreMultiplier());
        assertEquals(1, board.getSpace(12).getScoreMultiplier());
    }

    @Test
    void isValidId() {
        // No negative id's
        assertFalse(board.isIdValid(-10));

        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            assertTrue(board.isIdValid(i));
        }

        assertFalse(board.isIdValid(Board.BOARD_SIZE));
        assertFalse(board.isIdValid(Board.BOARD_SIZE + 120));
    }

    @Test
    void getBoardSpace() {
        assertNull(board.getSpace(-10));
        assertNull(board.getSpace(Board.BOARD_SIZE));

        assertEquals(0, board.getSpace(0).getId());
        assertEquals(12, board.getSpace(12).getId());
        assertEquals(Board.BOARD_SIZE - 1, board.getSpace(Board.BOARD_SIZE - 1).getId());
    }


    @Test
    void getTile() {
        assertNull(board.getTile(-10));
        assertNull(board.getTile(Board.BOARD_SIZE));

        // We have not placed a tile yet, so even a valid id should return null.
        assertNull(board.getTile(1));

        // TODO: make moves, and check that the tiles have been placed.
    }

    @Test
    void hasTile() {
        assertFalse(board.hasTile(-12));
        assertFalse(board.hasTile(Board.BOARD_SIZE));

        assertFalse(board.hasTile(1));
        assertFalse(board.hasTile(13));

        // TODO: make moves, and check that the tiles have been placed.
    }


    /**
     * Most of the checks for valid moves are done in the `makeMove` test.
     * This is because that test has to make moves to test other moves, and in the process tests the underlying
     * isMoveValid function.
     */
    @Test
    void isMoveValid() {
        Tile tile = new Tile(Color.BLUE, Color.PURPLE, Color.GREEN, 4);

        // Placing outside of the board is not a valid move.
        assertFalse(board.isMoveValid(-4, tile));
        assertFalse(board.isMoveValid(Board.BOARD_SIZE, tile));

        // Not placing a tile is an invalid move.
        assertFalse(board.isMoveValid(4, null));

        // Placing the first tile on a bonus space is invalid.
        assertFalse(board.isMoveValid(2, tile));
        assertFalse(board.isMoveValid(11, tile));

        // Valid moves.
        assertTrue(board.isMoveValid(3, tile));
        assertTrue(board.isMoveValid(8, tile));
        assertTrue(board.isMoveValid(24, tile));
    }

    @Test
    void makeMove() {
        Tile tile = new Tile(Color.BLUE, Color.PURPLE, Color.GREEN, 4);

        // Placing outside the board is invalid.
        assertThrows(InvalidMoveException.class, () -> board.makeMove(-3, tile));

        // Placing the first tile on a bonus space is invalid.
        assertThrows(InvalidMoveException.class, () -> board.makeMove(26, tile));
        assertThrows(InvalidMoveException.class, () -> board.makeMove(6, null));

        try {
            // Placing the first tile on a non-bonus space is valid.
            assertEquals(4, board.makeMove(12, tile));
            // The tile should now be there.
            assertEquals(tile, board.getTile(12));

            // Placing a tile where there is already a tile is invalid.
            assertThrows(InvalidMoveException.class, () -> board.makeMove(12, tile));

            // Placing tiles not adjacent to other tiles is invalid.
            assertThrows(InvalidMoveException.class, () -> board.makeMove(21, tile));
            assertThrows(InvalidMoveException.class, () -> board.makeMove(2, tile));
            assertThrows(InvalidMoveException.class, () -> board.makeMove(32, tile));

            // Placing a tile with non-matching colors is invalid.
            Tile tileWrongColor = new Tile(Color.RED, Color.RED, Color.YELLOW, 5);
            assertThrows(InvalidMoveException.class, () -> board.makeMove(6, tileWrongColor));
            assertThrows(InvalidMoveException.class, () -> board.makeMove(11, tileWrongColor));
            assertThrows(InvalidMoveException.class, () -> board.makeMove(13, tileWrongColor));

            // Placing a tile with matching colors is valid.
            Tile tileCorrectFlatColor = new Tile(Color.BLUE, Color.YELLOW, Color.RED, 3);
            assertEquals(3, board.makeMove(6, tileCorrectFlatColor));
            assertEquals(tileCorrectFlatColor, board.getTile(6));
            // Correct tile on a bonus space.
            Tile tileCorrectCwColor = new Tile(Color.YELLOW, Color.PURPLE, Color.RED, 2);
            assertEquals(8, board.makeMove(13, tileCorrectCwColor));
            assertEquals(tileCorrectCwColor, board.getTile(8));

            // A few more correct moves.
            assertEquals(1, board.makeMove(7, new Tile(Color.YELLOW, Color.GREEN, Color.RED, 1)));
            assertEquals(5, board.makeMove(14, new Tile(Color.YELLOW, Color.GREEN, Color.RED, 5)));

            // Tile matching on 2 sides should have double points.
            assertEquals(10, board.makeMove(8, new Tile(Color.YELLOW, Color.GREEN, Color.BLUE, 5)));

            // Joker is valid next to other colors. We are placing it next to a green side here (space 14).
            assertEquals(1, board.makeMove(15, new Tile(Color.WHITE, Color.WHITE, Color.WHITE, 1)));

            // TODO: Check for a tile matching on 3 sides.

        } catch (InvalidMoveException e) {
            e.printStackTrace();
        }

        // TODO: Maybe split this into multiple tests?
    }

    @Test
    void isEmpty() {
        assertTrue(board.getIsEmpty());

        try {
            board.makeMove(4, new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3));
        } catch (InvalidMoveException e) {
            e.printStackTrace();
        }

        assertFalse(board.getIsEmpty());
    }
}
