package ss.spec.gamepieces;

public class BoardCoordinates {
    private int row, column;

    public BoardCoordinates(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    /**
     * method to convert an index representation of a board field to a
     * coordinate representation.
     *
     * @param index Index value to be translated to coordinates
     * @return The points scored with this move.
     */
    public static BoardCoordinates fromIndex(int index) throws IndexException {

        if (index >= 0 && index < Board.BOARD_SIZE) {

            int row = (int) Math.floor((int) Math.sqrt((double) index));
            int column = index - (((int) Math.pow(row, 2)) + row);

            return new BoardCoordinates(row, column);

        } else {

            throw new IndexException(index);
        }
    }

    public int asIndex() throws IndexException {
        int index = getRow() + ((int) Math.pow(getRow(), 2)) + getColumn();

        if (index >= 0 && index < Board.BOARD_SIZE) {
            return index;
        } else {
            throw new IndexException(index);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass() != BoardCoordinates.class) {
            return false;
        }

        BoardCoordinates coords = (BoardCoordinates) other;

        return coords.getRow() == getRow() && coords.getColumn() == getColumn();
    }
}
