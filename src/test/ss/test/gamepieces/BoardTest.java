package ss.test.gamepieces;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.*;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void bonusSpaces() throws IndexException {
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
        assertFalse(Board.isIdValid(-10));

        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            assertTrue(Board.isIdValid(i));
        }

        assertFalse(Board.isIdValid(Board.BOARD_SIZE));
        assertFalse(Board.isIdValid(Board.BOARD_SIZE + 120));
    }

    @Test
    void getBoardSpace() throws IndexException {
        assertThrows(IndexException.class, () -> board.getSpace(-10));
        assertThrows(IndexException.class, () -> board.getSpace(Board.BOARD_SIZE));

        assertEquals(0, board.getSpace(0).getId());
        assertEquals(12, board.getSpace(12).getId());
        assertEquals(Board.BOARD_SIZE - 1, board.getSpace(Board.BOARD_SIZE - 1).getId());
    }


    @Test
    void getTile() {
        assertThrows(NoTileException.class, () -> board.getTile(-10));
        assertThrows(NoTileException.class, () -> board.getTile(Board.BOARD_SIZE));

        // We have not placed a tile yet, so even a valid id should throw exceptions.
        assertThrows(NoTileException.class, () -> board.getTile(1));
    }

    @Test
    void hasTile() {
        assertFalse(board.hasTile(-12));
        assertFalse(board.hasTile(Board.BOARD_SIZE));

        assertFalse(board.hasTile(1));
        assertFalse(board.hasTile(13));
    }


    /**
     * Most of the checks for valid moves are done in the `makeMove` test.
     * This is because that test has to make moves to test other moves, and in the process tests the underlying
     * isMoveValid function.
     */
    @Test
    void isMoveValid() {
        Tile tile = new Tile(Color.BLUE, Color.PURPLE, Color.GREEN, 4);

        Tile tile2 = new Tile(Color.BLUE, Color.BLUE, Color.GREEN, 4);

        assertTrue(board.isMoveValid(new Move(tile, 4)));


        assertTrue(board.isMoveValid(new Move(tile2, 21)));
        // Placing outside of the board is not a valid move.
        assertFalse(board.isMoveValid(new Move(tile, -4)));
        assertFalse(board.isMoveValid(new Move(tile, Board.BOARD_SIZE)));

        // Not placing a tile is an invalid move.
        assertFalse(board.isMoveValid(new Move(null, 4)));

        // Placing the first tile on a bonus space is invalid.
        assertFalse(board.isMoveValid(new Move(tile, 2)));
        assertFalse(board.isMoveValid(new Move(tile, 11)));

        // Valid moves.
        assertTrue(board.isMoveValid(new Move(tile, 3)));
        assertTrue(board.isMoveValid(new Move(tile, 8)));
        assertTrue(board.isMoveValid(new Move(tile, 24)));

        // Place the tile.
        board.placeTileDontCheckValidity(new Move(tile, 5));

        // Placing the second tile somewhere non-adjacent is invalid.
        assertFalse(board.isMoveValid(new Move(tile2, 22)));
        assertFalse(board.isMoveValid(new Move(tile2, 7)));

        // Try to place the other tile next to the first one.
        assertTrue(board.isMoveValid(new Move(tile2, 1)));
        assertTrue(board.isMoveValid(new Move(tile2, 4)));
        assertFalse(board.isMoveValid(new Move(tile2, 6)));
    }

    @Test
    void makeMove() throws InvalidMoveException, NoTileException {
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

        // Another correct move.
        assertEquals(1, board.makeMove(
                new Move(new Tile(Color.YELLOW, Color.GREEN, Color.RED, 1), 7)));
        // Double points for the bonus space.
        assertEquals(12, board.makeMove(
                new Move(new Tile(Color.PURPLE, Color.YELLOW, Color.RED, 6), 14)));

        // Tile matching on 2 sides should have double points.
        assertEquals(10, board.makeMove(
                new Move(new Tile(Color.PURPLE, Color.GREEN, Color.BLUE, 5), 8)));

        // Joker is valid next to other colors.
        // We are placing it next to a green side here (space 14).
        assertEquals(1, board.makeMove(
                new Move(new Tile(Color.WHITE, Color.WHITE, Color.WHITE, 1), 15)));
    }

    @Test
    void isEmpty() throws InvalidMoveException {
        assertTrue(board.getIsEmpty());

        board.makeMove(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 4));

        assertFalse(board.getIsEmpty());
    }

    @Test
    void getNumberOfAdjacentTiles() {
        assertEquals(0, board.getNumAdjacentTiles(6));

        board.placeTileDontCheckValidity(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 11));
        assertEquals(1, board.getNumAdjacentTiles(10));

        board.placeTileDontCheckValidity(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 4));
        assertEquals(2, board.getNumAdjacentTiles(10));

        board.placeTileDontCheckValidity(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 9));
        assertEquals(3, board.getNumAdjacentTiles(10));

        assertEquals(0, board.getNumAdjacentTiles(0));
    }

    @Test
    void adjacencyValid() {
        // Placing without adjacent tiles is invalid.
        assertFalse(board.adjacencyValid(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 11)));

        board.placeTileDontCheckValidity(new Move(new Tile(Color.RED, Color.PURPLE, Color.BLUE, 3), 11));
        assertTrue(board.adjacencyValid(new Move(new Tile(Color.RED, Color.GREEN, Color.YELLOW, 2), 19)));
    }
}
