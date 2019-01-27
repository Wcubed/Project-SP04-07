package ss.test.gamepieces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.*;

import java.util.ArrayList;
import java.util.List;

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
    void isMoveValid() throws InvalidMoveException {
        Tile tile = new Tile(Color.BLUE, Color.PURPLE, Color.GREEN, 4);

        Tile tile2 = new Tile(Color.BLUE, Color.BLUE, Color.GREEN, 4);

        assertTrue(board.isMoveValid(4, tile));             // testing if test on line 144 should really show false, I think not because no other tiles are
        // have been places, and it is not on a bonus field

        board.makeMove(new Move(tile, 13));

        assertTrue(board.isMoveValid(21, tile2));
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
    void makeMove() throws InvalidMoveException {
        Tile tile = new Tile(Color.BLUE, Color.PURPLE, Color.GREEN, 4);


        // Placing outside the board is invalid.
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(tile, -3)));

        // Placing the first tile on a bonus space is invalid.
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(tile, 26)));
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(null, 6)));


        // Placing the first tile on a non-bonus space is valid.
        assertEquals(4, board.makeMove(new Move(tile, 12)));
        // The tile should now be there.
        assertEquals(tile, board.getTile(12));

        // Placing a tile where there is already a tile is invalid.
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(tile, 12)));

        // Placing tiles not adjacent to other tiles is invalid.
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(tile, 4)));
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(tile, 2)));
        assertThrows(InvalidMoveException.class, () -> board.makeMove(new Move(tile, 32)));

        // Placing a tile with non-matching colors is invalid.
        Tile tileWrongColor = new Tile(Color.RED, Color.RED, Color.YELLOW, 5);
        assertThrows(InvalidMoveException.class, () ->
                board.makeMove(new Move(tileWrongColor, 6)));
        assertThrows(InvalidMoveException.class, () ->
                board.makeMove(new Move(tileWrongColor, 11)));
        assertThrows(InvalidMoveException.class, () ->
                board.makeMove(new Move(tileWrongColor, 13)));

        // Placing a tile with matching colors is valid.
        Tile tileCorrectFlatColor = new Tile(Color.BLUE, Color.YELLOW, Color.RED, 3);
        assertEquals(3, board.makeMove(new Move(tileCorrectFlatColor, 6)));
        assertEquals(tileCorrectFlatColor, board.getTile(6));
        // Correct tile on a bonus space.
        Tile tileCorrectCwColor = new Tile(Color.YELLOW, Color.PURPLE, Color.RED, 2);
        assertEquals(8, board.makeMove(new Move(tileCorrectCwColor, 13)));
        assertEquals(tileCorrectCwColor, board.getTile(13));

        // A few more correct moves.
        assertEquals(1, board.makeMove(
                new Move(new Tile(Color.YELLOW, Color.GREEN, Color.RED, 1), 7)));
        assertEquals(5, board.makeMove(
                new Move(new Tile(Color.YELLOW, Color.GREEN, Color.RED, 5), 4)));

        // Tile matching on 2 sides should have double points.
        assertEquals(10, board.makeMove(
                new Move(new Tile(Color.YELLOW, Color.GREEN, Color.BLUE, 5), 8)));

        // Joker is valid next to other colors.
        // We are placing it next to a green side here (space 14).
        assertEquals(1, board.makeMove(
                new Move(new Tile(Color.WHITE, Color.WHITE, Color.WHITE, 1), 15)));

        // TODO: Check for a tile matching on 3 sides.


        // TODO: Maybe split this into multiple tests?
    }

    @Test
    void isEmpty() throws InvalidMoveException {
        assertTrue(board.getIsEmpty());

        board.makeMove(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 4));

        assertFalse(board.getIsEmpty());
    }

    @Test
    void indexToCoordinates() throws IndexException {

        List a1 = new ArrayList<Integer>();
        a1.add(3);
        a1.add(1);

        List a2 = new ArrayList<Integer>();
        a2.add(4);
        a2.add(-2);


        // Testing the bound exception of input
        assertThrows(IndexException.class, () -> board.indexToCoordinates(37));
        assertThrows(IndexException.class, () -> board.indexToCoordinates(-3));

        assertEquals(a1, board.indexToCoordinates(13));
        assertEquals(a2, board.indexToCoordinates(18));

    }


}
