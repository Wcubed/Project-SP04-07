package ss.spec.gamepieces;

import ss.spec.networking.DecodeException;

public enum Color {
    RED, BLUE, GREEN, YELLOW, PURPLE, WHITE;


    /**
     * Checks whether this color is valid next to an other color.
     * WHITE is valid next to every color.
     *
     * @param other The other color to check against.
     * @return true when valid false when not.
     */
    public boolean isValidNextTo(Color other) {
        return this.equals(other) || this.equals(WHITE) || other.equals(WHITE);
    }


    public static Color decode(char c) throws DecodeException {
        switch (c) {
            case 'R':
                return Color.RED;
            case 'B':
                return Color.BLUE;
            case 'G':
                return Color.GREEN;
            case 'Y':
                return Color.YELLOW;
            case 'P':
                return Color.PURPLE;
            case 'W':
                return Color.WHITE;
            default:
                throw new DecodeException("Cannot create a Color from: \'" + c + "\'.");
        }
    }

    public String encode() {
        switch (this) {
            case RED:
                return "R";
            case BLUE:
                return "B";
            case GREEN:
                return "G";
            case YELLOW:
                return "Y";
            case PURPLE:
                return "P";
            case WHITE:
                return "W";
            default:
                return null;
        }
    }
}
