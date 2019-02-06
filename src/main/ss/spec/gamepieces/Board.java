package ss.spec.gamepieces;

import java.util.List;

public class Board {

    public static final int BOARD_SIZE = 36;

    private final BoardSpace[] spaces;
    private boolean isEmpty;

    public Board() {
        spaces = new BoardSpace[BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            int multiplier = 1;

            // Setup the bonus spaces.
            // Could be done using a dictionary, but there are only a few such spaces,
            // and they are not going to change during the project.
            // If it turns out that the spaces *do* change, we can turn it into a dictionary.
            if (i == 10 || i == 14 || i == 30) {
                multiplier = 2;
            } else if (i == 2 || i == 26 || i == 34) {
                multiplier = 3;
            } else if (i == 11 || i == 13 || i == 20) {
                multiplier = 4;
            }

            spaces[i] = new BoardSpace(i, multiplier);
        }

        isEmpty = true;
    }

    public BoardSpace getSpace(int id) throws IndexException {
        if (isIdValid(id)) {
            return spaces[id];
        } else {
            throw new IndexException();
        }
    }

    public Tile getTile(int id) throws NoTileException {
        if (hasTile(id)) {
            return spaces[id].getTile();
        } else {
            throw new NoTileException();
        }
    }

    public boolean hasTile(int id) {
        if (isIdValid(id)) {
            return spaces[id].hasTile();
        } else {
            return false;
        }
    }

    public static boolean isIdValid(int id) {
        return 0 <= id && id < BOARD_SIZE;
    }

    public boolean getIsEmpty() {
        return isEmpty;
    }

    /**
     * Returns true if a player with the given tiles would have a valid move to make.
     *
     * @param playerTiles The tiles in the player's hand.
     * @return true if the player can make a move, false otherwise.
     */
    public boolean hasValidMoves(List<Tile> playerTiles) {
        boolean result = false;

        for (Tile tile : playerTiles) {
            Tile rotated1 = tile.rotate120();
            Tile rotated2 = tile.rotate240();
            for (int id = 0; id < BOARD_SIZE; id++) {
                if (isMoveValid(new Move(tile, id)) ||
                        isMoveValid(new Move(rotated1, id)) ||
                        isMoveValid(new Move(rotated2, id))) {
                    // The player has at least 1 possible move.
                    result = true;
                    break;
                }
            }
            if (result) {
                break;
            }
        }

        return result;
    }

    public boolean isMoveValid(Move move) {
        boolean moveValid = false;

        if (move.getTile() != null) {
            try {
                if (getIsEmpty()) {
                    // First move cannot be placed on bonus tiles.
                    moveValid = !getSpace(move.getIndex()).isBonusSpace();
                } else {
                    // Space should be empty.
                    // The adjacency rules should be respected.
                    moveValid = !hasTile(move.getIndex()) &&
                            adjacencyValid(move);
                }
            } catch (IndexException e) {
                // Move invalid.
            }
        }

        return moveValid;
    }

    /**
     * Makes a move on the board, returns the points that this move scored.
     * <p>
     * fieldPoints = getScoreMultiplier of BoardSpace
     *
     * @param move The move to make.
     * @return The points scored with this move.
     */
    public int makeMove(Move move) throws InvalidMoveException {
        int id = move.getIndex();
        Tile tile = move.getTile();

        if (!isMoveValid(move)) {
            throw new InvalidMoveException(move);
        }

        int tilePoints = tile.getPoints(); // number of points inherent to the tile itself
        // Bonus for the field the tile is on.
        int fieldBonus = spaces[id].getScoreMultiplier();
        // Bonus acquired by number of adjacent sides
        int sideBonus = getNumAdjacentTiles(id);
        if (sideBonus == 0) {
            // 0 adjacent tiles will still get you normal points.
            sideBonus = 1;
        }
        int movePoints = tilePoints * fieldBonus * sideBonus;


        // Actually make the move.
        spaces[id].placeTile(tile);

        // If the board was empty, it for sure isn't now.
        isEmpty = false;

        return movePoints;
    }

    /**
     * Places a tile on a board space, without checking for validity of the move and
     * without calculating the score.
     * Will fail silently when given a move with invalid index, or a move without tile.
     *
     * @param move The move to make.
     */
    public void placeTileDontCheckValidity(Move move) {
        if (!Board.isIdValid(move.getIndex()) || move.getTile() == null) {
            return; // Silently fail.
        }

        spaces[move.getIndex()].placeTile(move.getTile());

        isEmpty = false;
    }

    /**
     * Returns the number of adjacent tiles.
     *
     * @param id - id of the field
     */
    public int getNumAdjacentTiles(int id) {
        int number = 0;

        try {
            BoardCoordinates coords = BoardCoordinates.fromIndex(id);

            try {
                if (hasTile(coords.getFlatNeighbourCoordinates().asIndex())) {
                    number += 1;
                }
            } catch (IndexException e) {
                // We expected this, don't do anything.
            }

            try {
                if (hasTile(coords.getClockwiseNeighbourCoordinates().asIndex())) {
                    number += 1;
                }
            } catch (IndexException e) {
                // We expected this, don't do anything.
            }

            try {
                if (hasTile(coords.getCounterclockwiseNeighbourCoordinates().asIndex())) {
                    number += 1;
                }
            } catch (IndexException e) {
                // We expected this, don't do anything.
            }

        } catch (IndexException e) {
            return 0;
        }

        return number;
    }

    /**
     * Checks whether the chosen move would violate any adjacency rules.
     * - The move should border at least 1 tile.
     * - All sides should match colors.
     *
     * @param move The move to evaluate.
     * @return True if the move respects adjacency rules.
     */
    public boolean adjacencyValid(Move move) {
        if (move == null) {
            return false;
        }

        if (getNumAdjacentTiles(move.getIndex()) <= 0) {
            // No adjacent tiles, by definition invalid.
            return false;
        }

        BoardCoordinates coords;
        try {
            coords = BoardCoordinates.fromIndex(move.getIndex());
        } catch (IndexException e) {
            return false;
        }

        boolean valid = true;

        try {
            // Check our flat side with the neighbouring flat side.
            if (!move.getTile().getFlatSide().isValidNextTo(
                    getTile(coords.getFlatNeighbourCoordinates().asIndex()).getFlatSide())) {
                valid = false;
            }
        } catch (IndexException | NoTileException e) {
            // We expected this, don't do anything.
        }

        try {
            // Check our clockwise side with the neighbouring clockwise side.
            if (!move.getTile().getClockwise1().isValidNextTo(
                    getTile(coords.getClockwiseNeighbourCoordinates().asIndex()).getClockwise1())) {
                valid = false;
            }
        } catch (IndexException | NoTileException e) {
            // We expected this, don't do anything.
        }

        try {
            // Check our counterclockwise side with the neighbouring counterclockwise side.
            if (!move.getTile().getClockwise2().isValidNextTo(
                    getTile(coords.getCounterclockwiseNeighbourCoordinates()
                            .asIndex()).getClockwise2())) {
                valid = false;
            }
        } catch (IndexException | NoTileException e) {
            // We expected this, don't do anything.
        }

        return valid;
    }
}



