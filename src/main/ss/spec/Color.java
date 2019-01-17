package ss.spec;

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


}
