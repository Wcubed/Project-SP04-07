package ss.spec.client;

import java.io.*;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Scanner;

public class TuiView implements SpecView {

    private ClientController controller;

    private BufferedReader in;
    private BufferedWriter out;

    private boolean running;

    private String lastPrompt;

    private boolean serverSupportsChat;
    private LinkedList<String> chatHistory;
    private int maxChatHistory;

    public TuiView(ClientController controller, Reader in, Writer out, boolean serverSupportsChat) {
        this.controller = controller;

        this.in = new BufferedReader(in);
        this.out = new BufferedWriter(out);

        this.serverSupportsChat = serverSupportsChat;

        lastPrompt = "";
        chatHistory = new LinkedList<>();
        maxChatHistory = 4;
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try {
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

    public void handleTerminalCommand(String command) {
        Scanner scanner = new Scanner(command);

        if (scanner.hasNext()) {
            String word = scanner.next();

            try {
                int number = Integer.decode(word);

                if (controller.canRequestGame()) {
                    controller.requestGame(number);
                }

                return;
            } catch (NumberFormatException e) {
                // Input is not a number, continue to check for commands.
            } catch (InvalidNumberException e) {
                // Whoops, that was not a valid number.
                // Show the player the prompt again.
                printPrompt();
                return;
            }

            switch (word) {
                case "help":
                    printHelpMessage();
                    break;
                case "exit":
                    controller.exitProgram();
                    break;
                case "chat":
                    if (serverSupportsChat) {
                        if (scanner.hasNextLine()) {
                            // Send the chat message.
                            // Remove unnecessary spaces.
                            controller.sendChatMessage(scanner.nextLine().trim());
                        }
                    } else {
                        println("Sorry, but the server does not support chat messages.");
                    }
                    break;

                default:
                    printHelpMessage();
                    print("> ");
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
        println("");
        println(lastPrompt);

        if (serverSupportsChat) {
            println("/-- Chat --------------------------");
            for (String message : chatHistory) {
                println("| " + message);
            }
            println("\\----------------------------------");
        }

        print("> ");
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public void addChatMessage(String name, String message) {
        chatHistory.addLast(name + ": " + message);

        while (chatHistory.size() > maxChatHistory) {
            chatHistory.removeFirst();
        }

        // Display the prompt along with the chat messages.
        printPrompt();
    }

    @Override
    public void closeView() {
        running = false;

        // TODO: would very much like to stop the tui input thread here.
        //       But trying to close the `in` buffer that it is stuck reading on does not work.
        //       Instead of throwing an exception on the other thread, as expected, it deadlocks.
        //       This also happens when trying to close the underlying `System.in`.
    }

    @Override
    public void update(Observable observable, Object o) {
        if (!o.getClass().equals(GameModel.Change.class) ||
                !observable.getClass().equals(GameModel.class)) {
            return;
        }

        GameModel.Change change = (GameModel.Change) o;
        GameModel model = (GameModel) observable;

        switch (change) {
            case TURN_ADVANCES:
                // TODO: Do something different when it's our turn.
                promptTurnAdvances(model);
        }
    }

    @Override
    public void promptGameRequest() {
        lastPrompt = "You are in the lobby. How many players do you want to play with?\n" +
                "Options: [2-4]";
        printPrompt();
    }

    public void promptTurnAdvances(GameModel model) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("It is now the turn of ");
        prompt.append(model.getCurrentTurnPlayer().getName());
        prompt.append(".\n");

        // TODO: Show the board and everything.

        lastPrompt = prompt.toString();
        printPrompt();
    }
}
