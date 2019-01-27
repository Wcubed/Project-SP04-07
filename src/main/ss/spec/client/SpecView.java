package ss.spec.client;

import ss.spec.gamepieces.Board;

import java.util.List;
import java.util.Observer;

public interface SpecView extends Observer, Runnable {

    void showBoard(Board board);

    void showTurnAdvance(List<String> turnOrder, Player currentPlayer);
}
