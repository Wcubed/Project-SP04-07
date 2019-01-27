package ss.spec.client;

import ss.spec.gamepieces.Board;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

public class TuiView implements SpecView {

    private BufferedReader in;
    private BufferedWriter out;

    private boolean running;

    public TuiView(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                handleTerminalCommand(in.readLine());
            } catch (IOException e) {
                // TODO: print nice message.
                e.printStackTrace();

                // No input anymore.
                running = false;
            }
        }
    }

    public void handleTerminalCommand(String command) throws IOException {
        Scanner scanner = new Scanner(command);

        if (scanner.hasNext()) {
            String word = scanner.next();

            if (word.equals("help")) {
                printHelpMessage();
                return;
            }

            if (word.equals("exit")) {
                running = false;

                return;
            }
        }
    }

    public void printHelpMessage() throws IOException {
        println("-----------------");
        println("help -> Print this help message.");
        println("exit -> Exit the program.");
    }

    public void print(String message) throws IOException {
        out.write(message);
    }

    public void println(String message) throws IOException {
        out.write(message + "\n");
        out.flush();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o.getClass().equals(GameModel.Change.class)) {
            return;
        }

        GameModel.Change change = (GameModel.Change) o;

        switch (change) {
            case TURN_ADVANCES:
                break;
        }
    }

    @Override
    public void showBoard(Board board) {

    }

    @Override
    public void showTurnAdvance(List<String> turnOrder, Player currentPlayer) {
        try {
            print("Turn order: ");

            for (String name : turnOrder) {
                print(name);
                print(" ");
            }

            println("");
            print("It is now the turn of ");
            print(currentPlayer.getName());
            println(".");

        } catch (IOException e) {
            // TODO: what to do here?
            e.printStackTrace();
        }
    }
}
