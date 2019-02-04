package ss.spec.client;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.Tile;

import java.io.*;
import java.util.*;

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

                // Let the controller decide what to do with the number.
                controller.userInputNumber(number);

                return;
            } catch (NumberFormatException e) {
                // Input is not a number, continue to check for commands.
            } catch (InvalidNumberException e) {
                // Whoops, that was not a valid number!
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
                promptTurnAdvances(model);
                break;
            case TURN_ADVANCES_OUR_TURN:
                // It's the start of our turn now.
                if (model.getState().equals(GameModel.State.MAKE_MOVE_DECIDE_TILE)) {
                    promptTileChoice(model);
                } else {
                    promptSkipChoice(model);
                }
                break;
            case MOVE_DECISION_PROGRESS:
                // We are in the process of planning a move.
                switch (model.getState()) {
                    case MAKE_MOVE_DECIDE_TILE:
                        promptTileChoice(model);
                        break;
                    case MAKE_MOVE_DECIDE_BOARD_SPACE:
                        promptBoardSpaceChoice(model);
                        break;
                    case MAKE_MOVE_DECIDE_ORIENTATION:
                        promptTileOrientationChoice(model);
                        break;
                }
                break;
            case INVALID_MOVE_ATTEMPTED:
                promptTileChoiceAfterInvalidMove(model);
                break;
        }
    }

    @Override
    public void promptGameRequest() {
        lastPrompt = "You are in the lobby. How many players do you want to play with?\n" +
                "Options: [2-4]";
        printPrompt();
    }

    @Override
    public void promptWaitingForGame(List<String> names) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Waiting for a game...\n");
        prompt.append("Also waiting: ");

        for (String name : names) {
            prompt.append(name);
            prompt.append(", ");
        }

        lastPrompt = prompt.toString();
        printPrompt();
    }

    private void promptTurnAdvances(GameModel model) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(boardAsString(model.getBoard()));

        prompt.append("\n");

        prompt.append("It is now the turn of ");
        prompt.append(model.getCurrentTurnPlayer().getName());
        prompt.append(".\n");

        prompt.append("The turn order, and current points are:\n");
        for (String name : model.getTurnOrder()) {
            prompt.append(name);
            prompt.append(" ");
            prompt.append(model.getPlayers().get(name).getScore());
            prompt.append(" -> ");
        }
        prompt.append("new round.");

        lastPrompt = prompt.toString();
        printPrompt();
    }

    public void promptTileChoice(GameModel model) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(boardAsString(model.getBoard()));
        prompt.append("\n");
        prompt.append("It is your turn!\n");

        prompt.append(tileListAsString(model.getLocalPlayer().getTiles()));

        prompt.append("Which tile would you like to place? ");
        prompt.append("[0-" + (model.getLocalPlayer().getTiles().size() - 1) + "]\n");

        lastPrompt = prompt.toString();
        printPrompt();
    }

    private void promptTileChoiceAfterInvalidMove(GameModel model) {
        // TODO: there is probably a better way to do these prompts.
        //  As a lot of stuff is the same every time...
        StringBuilder prompt = new StringBuilder();

        prompt.append(boardAsString(model.getBoard()));
        prompt.append("\n");
        prompt.append("Sorry but that move is invalid, please try again...\n");

        prompt.append(tileListAsString(model.getLocalPlayer().getTiles()));

        prompt.append("Which tile would you like to place? ");
        prompt.append("[0-" + (model.getLocalPlayer().getTiles().size() - 1) + "]\n");

        lastPrompt = prompt.toString();
        printPrompt();
    }

    private void promptBoardSpaceChoice(GameModel model) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(boardAsString(model.getBoard()));
        prompt.append("\n");

        List<Tile> tile = new ArrayList<>();
        tile.add(model.getSelectedTile());

        prompt.append(tileListAsString(tile));

        prompt.append("Where do you want to place your tile? ");
        prompt.append("[0-" + (Board.BOARD_SIZE - 1) + "]\n");

        // TODO: Allow for CANCEL!

        lastPrompt = prompt.toString();
        printPrompt();
    }

    private void promptTileOrientationChoice(GameModel model) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(boardAsString(model.getBoard()));
        prompt.append("\n");

        List<Tile> orientations = new ArrayList<>();
        orientations.add(model.getSelectedTile());
        orientations.add(model.getSelectedTile().rotate120());
        orientations.add(model.getSelectedTile().rotate240());

        // TODO: Maybe display them upside-down if the selected board space is oriented that way?
        prompt.append(tileListAsString(orientations));

        prompt.append("In which orientation do you want to place the tile? ");
        prompt.append("[0-3]\n");

        // TODO: Allow for CANCEL!

        lastPrompt = prompt.toString();
        printPrompt();
    }

    public void promptSkipChoice(GameModel model) {
        // TODO: implement skipping or replace.
    }

    private String boardAsString(Board board) {
        List<Integer> values = new ArrayList<>();
        List<Character> flat = new ArrayList<>();
        List<Character> cw = new ArrayList<>();
        List<Character> ccw = new ArrayList<>();

        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            Tile tile = board.getTile(i);

            if (tile != null) {
                values.add(tile.getPoints());
                flat.add(tile.getFlatSide().encode());
                cw.add(tile.getClockwise1().encode());
                ccw.add(tile.getClockwise2().encode());
            } else {
                values.add(null);
                flat.add(null);
                cw.add(null);
                ccw.add(null);
            }
        }

        return SpectrangleBoardPrinter.getBoardString(values, flat, cw, ccw);
    }

    private String tileListAsString(List<Tile> tiles) {
        ArrayList<StringBuilder> lines = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            lines.add(new StringBuilder());
        }

        for (Tile tile : tiles) {
            lines.get(0).append("     ^     ");
            lines.get(1).append("    / \\    ");
            lines.get(2).append("   /   \\   ");
            lines.get(3).append(String.format("  /%c %1d %c\\  ",
                    tile.getClockwise1().encode(),
                    tile.getPoints(),
                    tile.getClockwise2().encode()));
            lines.get(4).append(String.format(" /   %c   \\ ", tile.getFlatSide().encode()));
            lines.get(5).append("/---------\\");
        }

        StringBuilder output = new StringBuilder();

        for (StringBuilder line : lines) {
            output.append(line);
            output.append("\n");
        }

        return output.toString();
    }
}
