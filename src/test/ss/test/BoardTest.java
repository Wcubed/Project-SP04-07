package ss.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.Board;
import ss.spec.Color;
import ss.spec.InvalidMoveException;
import ss.spec.Tile;

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

        // TODO: test making moves next to already placed tiles.
        // TODO: maybe split this test into multiple ones.
    }

    @Test
    void makeMove() {
        Tile tile = new Tile(Color.BLUE, Color.PURPLE, Color.GREEN, 4);

        assertThrows(InvalidMoveException.class, () -> board.makeMove(-3, tile));
        assertThrows(InvalidMoveException.class, () -> board.makeMove(26, tile));
        assertThrows(InvalidMoveException.class, () -> board.makeMove(6, null));

        // TODO: Make some valid moves.
        // TODO: Make some more types of invalid moves.
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
