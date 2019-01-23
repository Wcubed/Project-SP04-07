package ss.spec.gamepieces;

public class IndexException extends Exception {

    private int index;

    public IndexException(int index) {
        this.index = index;
    }

    public String toString() {
        return "IndexException" + "[" + index + "] \n Id out of bounds";
    }

}

