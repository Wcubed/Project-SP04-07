package ss.spec.client;

import ss.spec.gamepieces.Board;

import java.io.*;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

public class TuiView implements SpecView {

    private ClientController controller;

    private BufferedReader in;
    private BufferedWriter out;

    private boolean running;

    public TuiView(ClientController controller, Reader in, Writer out) {
        this.controller = controller;

        this.in = new BufferedReader(in);
        this.out = new BufferedWriter(out);
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                print("> ");
                String command = in.readLine();

                if (command != null) {
                    handleTerminalCommand(command);
                } else {
                    // End of stream, input has been closed.
                    running = false;
                }
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
                controller.exitProgram();
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
        out.flush();
    }

    public void println(String message) throws IOException {
        out.write(message + "\n");
        out.flush();
    }

    @Override
    public void closeView() {
        running = false;

        // TODO: would very much like to stop the tui input thread here.
        //       But trying to close the `in` buffer that it is stuck reading on does not work.
        //       Instead of throwing an exception on the other thread, as expected, it deadlocks.
        //       This also happens when trying to close the underlying `System.in`.
    }

    // ---------------------------------------------------------------------------------------------

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
