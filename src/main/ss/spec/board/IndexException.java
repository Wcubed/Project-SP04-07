package ss.spec.board;

public class IndexException extends Exception{

    int index;

    public IndexException(int index){
        this.index = index;
    }

    public String toString(){
        return "IndexException" + "[" + index + "] \n Id out of bounds";
    }

}

