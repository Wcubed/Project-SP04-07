package ss.spec.gamepieces;

import ss.spec.networking.DecodeException;

/**
 * Represents a Tile on the game gamepieces.
 * Tiles have three colored sides, named "flatSide", "clockwise1" and "clockwise2".
 * They area arranged as follows:
 *
 * <pre>
 * {@code
 *     /\   \-flat-/   /\
 * cw1/..\cw2\..../cw1/..\cw2
 *   /....\   \../   /....\
 *  /_flat_\   \/   /_flat_\
 *
 *  \-flat-/   /\   \-flat-/
 *   \..../   /..\   \..../
 * cw2\../cw1/....\cw2\../cw1
 *     \/   /_flat_\   \/
 * }
 * </pre>
 * <p>
 * This way, all bordering sides have the same name, so that one can do:
 * <code>tile1.getClockwise1().isValidNextTo(tile2.getClockwise1())</code>
 * regardless of tile orientation.
 */
public class Tile {
    private Color flatSide;
    private Color clockwise1;
    private Color clockwise2;

    private int points;

    public Tile(Color flatSide, Color clockwise1, Color clockwise2, int points) {
        this.flatSide = flatSide;
        this.clockwise1 = clockwise1;
        this.clockwise2 = clockwise2;
        this.points = points;
    }

    /**
     * Creates a copy of the tile.
     */
    public Tile(Tile other) {
        this.flatSide = other.flatSide;
        this.clockwise1 = other.clockwise1;
        this.clockwise2 = other.clockwise2;
        this.points = other.points;
    }

    /**
     * Returns a copy of the tile that is rotated 120 degrees clockwise.
     */
    public Tile rotate120() {
        return new Tile(clockwise2, flatSide, clockwise1, points);
    }

    /**
     * Returns a copy of the tile that is rotated 240 degrees clockwise.
     * (the equivalent of rotating 120 degrees counterclockwise.)
     */
    public Tile rotate240() {
        return new Tile(clockwise1, clockwise2, flatSide, points);
    }

    /**
     * Tests whether two tiles are equivalent.
     * Meaning: the one tile can be rotated so that it equals the other tile.
     *
     * @param other The tile to test equivalency with.
     * @return Whether the tiles are equivalent or not.
     */
    public boolean isEquivalent(Tile other) {
        return this.equals(other) ||
                this.equals(other.rotate120()) ||
                this.equals(other.rotate240());
    }

    /**
     * Test for equality. Rotated tiles are not equal to each other.
     *
     * @param o The object to test equality with.
     * @return Whether the two objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        Tile other = (Tile) o;
        return getFlatSide().equals(other.getFlatSide()) &&
                getClockwise1().equals(other.getClockwise1()) &&
                getClockwise2().equals(other.getClockwise2()) &&
                getPoints() == other.getPoints();
    }

    public Color getFlatSide() {
        return flatSide;
    }

    public Color getClockwise1() {
        return clockwise1;
    }

    public Color getClockwise2() {
        return clockwise2;
    }

    public int getPoints() {
        return points;
    }

    public static Tile decode(String message) throws DecodeException {
        if (message == null || message.length() != 4) {
            throw new DecodeException("Cannot create Tile from message: \'" + message + "\'.");
        }

        //try {
        Color flat = Color.decode(message.charAt(0));
        Color cw1 = Color.decode(message.charAt(1));
        Color cw2 = Color.decode(message.charAt(2));
        int points = Character.getNumericValue(message.charAt(3));

        if (points <= 0) {
            throw new DecodeException("Cannot create Tile from message: \'" + message + "\'.");
        }

        return new Tile(flat, cw1, cw2, points);
    }

    public String encode() {
        return flatSide.encode() +
                clockwise1.encode() +
                clockwise2.encode() +
                points;
    }

}
