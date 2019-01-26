package ss.spec.gamepieces;

public class InvalidMoveException extends Exception {

    int index;

    public InvalidMoveException(int index) {
        this.index = index;
    }

    public String toString() {
        return "InvalidMoveException: Move on" + "[" + index + "] + not valid";
    }

}
