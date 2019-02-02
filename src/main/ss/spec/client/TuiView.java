package ss.spec.client;

import ss.spec.gamepieces.Board;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

public class TuiView implements SpecView {

    private ClientController controller;

    private BufferedReader in;
    private BufferedWriter out;

    private boolean running;

    private String lastPrompt;

    private boolean serverSupportsChat;
    private ArrayList<String> chatHistory;

    public TuiView(ClientController controller, Reader in, Writer out, boolean serverSupportsChat) {
        this.controller = controller;

        this.in = new BufferedReader(in);
        this.out = new BufferedWriter(out);

        this.serverSupportsChat = serverSupportsChat;

        lastPrompt = "";
        chatHistory = new ArrayList<>();
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

            switch (word) {
                case "help":
                    printHelpMessage();
                    break;
                case "exit":
                    controller.exitProgram();
                    break;
                case "chat":
                    if (serverSupportsChat) {
                        // TODO: Implement chat?.
                    } else {
                        println("Sorry, but the server does not support chat messages.");
                    }
                    break;

                default:
                    printHelpMessage();
            }
        }
    }

    private void printHelpMessage() {
        println("-----------------");
        println("help -> Print this help message.");
        println("exit -> Exit the program.");

        if (serverSupportsChat) {
            println("chat -> Send a chat message.");
        }
    }

    private void print(String message) {
        try {
            out.write(message);
            out.flush();
        } catch (IOException e) {
            running = false;
        }
    }

    private void println(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            running = false;
        }
    }

    private void printPrompt() {
        println(lastPrompt);

        if (serverSupportsChat) {
            println("/-- Chat ----------------\\");
            for (String message : chatHistory) {
                println("| " + message);
            }
            println("\\------------------------/");
        }
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

    public void showBoard(Board board) {

    }

    public void showTurnAdvance(List<String> turnOrder, Player currentPlayer) {
        // TODO: Put this in the lastPrompt string.

        print("Turn order: ");

        for (String name : turnOrder) {
            print(name);
            print(" ");
        }

        println("");
        print("It is now the turn of ");
        print(currentPlayer.getName());
        println(".");
    }
}
