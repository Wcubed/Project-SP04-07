package ss.spec.gamepieces;

import java.util.ArrayList;

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

    public boolean isIdValid(int id) {
        return 0 <= id && id < BOARD_SIZE;
    }

    public boolean getIsEmpty() {
        return isEmpty;
    }

    public boolean isMoveValid(int id, Tile tile) {
        boolean moveValid = false;

        if (isIdValid(id) && tile != null) {
            if (getIsEmpty()) {
                // First move cannot be placed on bonus tiles.
                moveValid = !getSpace(id).isBonusSpace();
            } else {

                // Check if the space is empty.
                if (!hasTile(id)) {
                    moveValid = true;
                }

                // TODO: check for colors.

            }
        }

        return moveValid;
    }

    /**
     * Makes a move on the gamepieces, returns the points that this move scored.
     *
     * @param id   The space to place the tile on.
     * @param tile The tile to place.
     * @return The points scored with this move.
     */
    public int makeMove(int id, Tile tile) throws InvalidMoveException {
        if (!isMoveValid(id, tile)) {
            throw new InvalidMoveException();
        }

        spaces[id].placeTile(tile);

        isEmpty = false;

        // TODO: properly multiply the points.

        return tile.getPoints();
    }


    /**
     * method to convert an index representation of a gamepieces field to a
     * coordinate representation of the form (r,c).
     *
     * @param index Index value to be translated to coordinates
     * @return The points scored with this move.
     */

    public ArrayList indexToCoordinates(int index) throws IndexException {

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

}


