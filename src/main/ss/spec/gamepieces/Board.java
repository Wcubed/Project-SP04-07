package ss.spec.gamepieces;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final int BOARD_SIZE = 36;

    private BoardSpace[] spaces;
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

    public BoardSpace getSpace(int id) {
        if (isIdValid(id)) {
            return spaces[id];
        } else {
            return null;
        }
    }

    public Tile getTile(int id) {
        if (isIdValid(id)) {
            return spaces[id].getTile();
        } else {
            return null;
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

        return true;
    }

    public boolean isMoveValid(Move move) {
        boolean moveValid = false; // Initialized to false prevent "variable might have not been initialized error by intellij"

        if (isIdValid(move.getIndex()) && move.getTile() != null) {
            if (getIsEmpty()) {
                // First move cannot be placed on bonus tiles.
                moveValid = !getSpace(move.getIndex()).isBonusSpace();
            } else {
                // Check if the space is empty.
                if (!hasTile(move.getIndex())) {
                    if (colorsValid(move.getIndex(), move.getTile())) {
                        moveValid = true;
                    }
                }

            }
        }

        if (!getIsEmpty() && getNumSides(move.getIndex()) == 0) {
            moveValid = false;
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
        int fieldPoints = spaces[id].getScoreMultiplier(); // points of field that tile is placed on
        int sidePoints = getNumSides(id);      // points acquired by number of adjacent sides
        if (sidePoints == 0) {
            sidePoints = 1;
        }
        int movePoints = tilePoints * fieldPoints * sidePoints;


        spaces[id].placeTile(tile);

        isEmpty = false;

        // Notes: Points of move = points of tile * points of field * number of matching sides.

        return movePoints;
    }

    /**
     * method to convert an index representation of a board field to a
     * coordinate representation of the form (r,c)
     *
     * @param index Index value to be translated to coordinates
     * @return The points scored with this move.
     */

    public ArrayList<Integer> indexToCoordinates(int index) throws IndexException {

        ArrayList<Integer> result = new ArrayList<Integer>();
        int r;
        int c;

        if (index <= 36 && index >= 0) {

            r = ((int) Math.floor((int) Math.sqrt((double) index)));
            c = (index - (((int) Math.pow(r, 2)) + r));

            result.add(r);
            result.add(c);

            return result;

        } else {

            throw new IndexException(index);
        }

    }

    public int coordinatesToIndex(int r, int c) {
        int index = (r + ((int) Math.pow(r, 2)) + c);
        return index;
    }

    /**
     * @param id - id of the field
     *           <p>
     *           Function that returns the number of sides of
     *           sides where there is another Tile adjacent to it
     **/

    public int getNumSides(int id) {
        ArrayList coordinates = null;  //  !!! Initialized to null to prevent: "x might have not been initialized error in intellij"

        try {
            coordinates = indexToCoordinates(id);
        } catch (IndexException e) {
            e.printStackTrace();
        }

        int r = (int) coordinates.get(0);
        int c = (int) coordinates.get(1);

        int sides = 0;

        boolean hasBottom = false;

        // Checking bottom

        if ((r + c) % 2 == 0) {
            hasBottom = true;
        }


        // Checking right neighbor

        if ((c + 1) <= r) {
            if (hasTile(coordinatesToIndex(r, c + 1))) {
                sides += 1;
            }
        }

        return sides;
    }

    public boolean colorsValid(int id, Tile tile) {

        /**
         *
         *  right neighbor ->  non existant if c + 1 > r
         *
         *  left neighbor -> non existant if c - 1 < -r
         *
         *  Top neighbour exists -> (when r + c is uneven)
         *
         *  Bottom neighbour ->(if r + c is even)
         *
         *  THERE IS EITHER A BOTTOM OR A TOP NEIGHBOR
         *
         *  SEE PDF for other used relations
         *
         *
         **/

        ArrayList<Integer> coordinates = null;  //  !!! Initialized to null to prevent: "x might have not been initialized error in intellij"

        Tile rightAdjacent;
        Tile leftAdjacent;
        Tile bottomAdjacent;
        Tile topAdjacent;

        boolean rightMatch;
        boolean leftMatch;
        boolean topMatch;
        boolean bottomMatch;

        try {
            coordinates = indexToCoordinates(id);
        } catch (IndexException e) {
            e.printStackTrace();
        }

        int r = (int) coordinates.get(0);
        int c = (int) coordinates.get(1);


        boolean hasBottom = false;

        // Checking bottom

        if ((r + c) % 2 == 0) {
            hasBottom = true;
        }


        // Checking right neighbor

        if ((c + 1) <= r) {
            if (hasTile(coordinatesToIndex(r, c + 1))) {
                rightAdjacent = getTile(coordinatesToIndex(r, c + 1));
                rightMatch = tile.getClockwise2().isValidNextTo(rightAdjacent.getClockwise2());
            } else {
                rightMatch = true;
            }
        } else {

            rightMatch = true;
        }

        // Checking left neighbor

        if ((c - 1) >= (-r)) {
            if (hasTile(coordinatesToIndex(r, c - 1))) {
                leftAdjacent = getTile(coordinatesToIndex(r, c - 1));
                leftMatch = tile.getClockwise1().isValidNextTo(leftAdjacent.getClockwise1());
            } else {
                leftMatch = true;

            }
        } else {

            leftMatch = true;

        }
        if (hasBottom) {
            // Check for bottom neighbor
            if (hasTile(coordinatesToIndex(r + 1, c))) {
                bottomAdjacent = getTile(coordinatesToIndex(r + 1, c));
                bottomMatch = tile.getFlatSide().isValidNextTo(bottomAdjacent.getFlatSide());
                topMatch = true;
            } else {
                bottomMatch = true;
                topMatch = true;

            }

        } else {
            // Check for top neighbor
            if (hasTile(coordinatesToIndex(r - 1, c))) {
                topAdjacent = getTile(coordinatesToIndex(r - 1, c));
                topMatch = tile.getFlatSide().isValidNextTo(topAdjacent.getFlatSide());
                bottomMatch = true;
            } else {
                bottomMatch = true;
                topMatch = true;
            }

        }

        if (!leftMatch || !rightMatch || !bottomMatch || !topMatch) {
            return false;
        } else {
            return true;
        }
    }
}



