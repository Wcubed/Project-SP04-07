package ss.spec.gamepieces;

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

}
