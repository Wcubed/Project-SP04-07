package ss.spec;

/**
 * Represents a Tile on the game board.
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
     * Rotates the tile 120 degrees clockwise.
     */
    public void rotate120() {
        Color temp = flatSide;
        flatSide = clockwise2;
        clockwise2 = clockwise1;
        clockwise1 = temp;
    }

    /**
     * Rotates the tile 240 degrees clockwise.
     * (the equivalent of rotating 120 degrees counterclockwise.)
     */
    public void rotate240() {
        Color temp = flatSide;
        flatSide = clockwise1;
        clockwise1 = clockwise2;
        clockwise2 = temp;
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
