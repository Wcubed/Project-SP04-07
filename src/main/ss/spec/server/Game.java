package ss.spec.server;

import ss.spec.gamepieces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Game implements Runnable {

    private boolean gameOver;

    private final ArrayList<Player> players;
    private final Board board;
    private final TileBag bag;

    private final ArrayList<String> turnOrder;
    private int currentTurnPlayer;

    /**
     * Instantiates the Game class with the given players, board and TileBag.
     *
     * @param players The participating players, order doesn't matter.
     * @param board   The board to play on.
     * @param bag     The tile bag to use.
     */
    public Game(List<ClientPeer> players, Board board, TileBag bag) {
        this.players = new ArrayList<>();

        for (ClientPeer peer : players) {
            this.players.add(new Player(peer));
        }

        this.board = board;
        this.bag = bag;

        gameOver = false;

        this.turnOrder = new ArrayList<>();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void gameIsNowOver() {
        gameOver = true;
    }

    /**
     * @return The list of players.
     */
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public void run() {
        setUpGame();

        while (!isGameOver()) {
            doSingleGameThreadIteration();

            // Sleep a while, we don't need sub-millisecond responses on this game.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Does everything needed to get ready for a game.
     * Initializes the tile bag
     * Determines the turn order.
     * Makes sure everyone has 4 tiles.
     */
    public void setUpGame() {
        bag.addAllStartingTiles();

        decideTurnOrder();

        // Fill the client's tiles till everyone has 4.
        for (Player player : players) {
            List<Tile> tiles = player.getTiles();
            // The turn order procedure can leave players with more than 4 tiles.
            while (tiles.size() > Player.MAX_HAND_SIZE) {
                // Put them back in the bag.
                bag.addTile(tiles.remove(tiles.size() - 1));
            }
            while (tiles.size() < Player.MAX_HAND_SIZE) {
                attemptDrawTileForPlayer(player);
            }
        }

        // Let the clients know the game is on.
        for (Player player : players) {
            player.getPeer().sendStartMessage(turnOrder);
            player.getPeer().sendOrderMessage(turnOrder);

            player.getPeer().awaitTurn();
        }
    }


    public void doSingleGameThreadIteration() {
        for (Player player : players) {
            ClientPeer peer = player.getPeer();

            if (!player.isPeerConnected()) {
                stopGamePlayerDisconnected(player.getName());
                break;
            }


            if (player.getName().equals(getCurrentTurnPlayerName())) {
                // It's this player's turn.

                switch (peer.getState()) {
                    case GAME_AWAITING_TURN:

                        if (board.hasValidMoves(player.getTiles())) {
                            peer.clientDecideMove();
                            // Notify everyone that this player's turn has started.
                            sendTileAndTurnAnnouncement(player.getName());
                        } else {
                            // Whoops, no move for this player.
                            peer.clientDecideSkip();
                            // Announce that this player does not have a valid move option.
                            sendSkipAnnouncement(player.getName());
                        }
                        break;

                    case PEER_DECIDE_MOVE:
                        // Waiting for the peer to send a move message.
                        // Do nothing.
                        break;
                    case GAME_VERIFY_MOVE:

                        Move move = peer.getProposedMove();

                        if (move == null) {
                            // No move, try again.
                            peer.invalidMove();
                            break;
                        }
                        if (!player.hasTileInHand(move.getTile())) {
                            // You can't place a tile you don't have in hand.
                            peer.invalidMove();
                            break;
                        }

                        try {
                            // Now we can make the move.
                            int points = board.makeMove(move);

                            player.addPoints(points);
                            player.removeTile(move.getTile());

                            sendMoveAnnouncement(player.getName(), move, points);

                            // Move is valid, the turn goes to the next player.
                            attemptDrawTileForPlayer(player);
                            peer.awaitTurn();
                            advanceTurnPlayer();
                        } catch (InvalidMoveException e) {
                            // Invalid move, try again.
                            peer.invalidMove();
                        }
                        break;

                    case PEER_DECIDE_SKIP:
                        // Peer is deciding whether to skip or not.
                        // Do nothing.
                        break;
                    case GAME_VERIFY_SKIP:

                        Tile replaceTile = peer.getProposedReplaceTile();

                        if (replaceTile == null) {
                            // Player wants to skip.
                            sendSkipAnnouncement(player.getName());
                        } else {
                            // Player wants to replace a tile.

                            if (!player.hasTileInHand(replaceTile)) {
                                // You can't replace a tile you don't have in hand.
                                peer.invalidMove();
                                break;
                            }

                            // Ready to replace.
                            player.removeTile(replaceTile);
                            Tile drawnTile = attemptDrawTileForPlayer(player);
                            sendReplaceAnnouncement(player.getName(), replaceTile, drawnTile);
                        }

                        peer.awaitTurn();
                        advanceTurnPlayer();
                        break;
                    default:
                        // This is a weird state to be in. Shouldn't happen.
                        // Awaiting turn is a save state, so put them in that.
                        peer.awaitTurn();
                }
            }
        }

        // Handle chat.
        distributeChatMessages();

        // Check if the game is over.
        if (bag.getNumTilesLeft() == 0) {
            // No tiles left in the bag.
            // Are there any players who can make a move?
            boolean noOneCanMove = true;
            for (Player player : players) {
                if (board.hasValidMoves(player.getTiles())) {
                    // Hey! someone can still move.
                    // The game goes on.
                    noOneCanMove = false;
                    break;
                }
            }

            if (noOneCanMove) {
                // Uh oh, game over!
                stopGameNoMovesLeft();
            }
        }
    }

    /**
     * Will draw tiles and use the point values to determine the players' turn order.
     * Equal points are resolved by having the tied players draw again until there is no tie.
     * Players can end up with more than 4 tiles after this procedure.
     */
    private void decideTurnOrder() {
        ArrayList<Player> noTurnYet = new ArrayList<>();

        for (Player player : players) {
            noTurnYet.add(player);
            // Draw a tile.
            try {
                player.addTileToHand(bag.takeTile());
            } catch (EmptyTileBagException e) {
                // This should not be possible.
                e.printStackTrace();
            }
        }

        while (!noTurnYet.isEmpty()) {
            int highest = Integer.MIN_VALUE;
            int tiedPoints = Integer.MIN_VALUE;
            Player highestPlayer = noTurnYet.get(0);

            for (Player player : noTurnYet) {
                List<Tile> tiles = player.getTiles();

                int points = tiles.get(tiles.size() - 1).getPoints();
                if (points > highest) {
                    highest = points;
                    highestPlayer = player;
                } else if (points == highest) {
                    // The highest is potentially a tie.
                    tiedPoints = points;
                }
            }

            if (highest > tiedPoints) {
                // No tie for highest, let's add the player to the turn order.
                turnOrder.add(highestPlayer.getName());
                noTurnYet.remove(highestPlayer);
            } else {
                // Two or more players have the highest value.
                // Have them draw again.
                for (Player player : noTurnYet) {
                    List<Tile> tiles = player.getTiles();

                    int points = tiles.get(tiles.size() - 1).getPoints();

                    if (points == tiedPoints) {
                        try {
                            tiles.add(bag.takeTile());
                        } catch (EmptyTileBagException e) {
                            // This should not be possible.
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        currentTurnPlayer = 0;
    }

    private String getCurrentTurnPlayerName() {
        return turnOrder.get(currentTurnPlayer);
    }

    /**
     * Set's the next player as being the one who's turn it is.
     */
    private void advanceTurnPlayer() {
        currentTurnPlayer++;
        currentTurnPlayer %= turnOrder.size();
    }

    /**
     * Will attempt to draw a tile for the given player.
     * Will not allow a player to have more than 4 tiles.
     * Will not crash on an empty bag.
     * All of the cases above are handled gracefully.
     *
     * @param player The player to draw a tile for.
     * @return A copy of the tile the player got, null if there are no tiles left in the bag.
     */
    private Tile attemptDrawTileForPlayer(Player player) {
        List<Tile> tiles = player.getTiles();
        Tile tile = null;

        if (tiles.size() < Player.MAX_HAND_SIZE) {
            try {
                tile = bag.takeTile();
                // Make sure we copy the tile.
                tiles.add(new Tile(tile));
            } catch (EmptyTileBagException e) {
                // Empty bag.
                // No need to do anything.
            }
        }

        return tile;
    }

    /**
     * Let's all the players know who has what tiles, and who's turn it is.
     *
     * @param playerName The player who's turn it is.
     */
    private void sendTileAndTurnAnnouncement(String playerName) {
        StringBuilder message = new StringBuilder();

        message.append("tiles ");

        for (Player player : players) {
            message.append(player.getName());
            message.append(" ");

            for (int i = 0; i < 4; i++) {
                try {
                    message.append(player.getTiles().get(i).encode());
                    message.append(" ");
                } catch (IndexOutOfBoundsException e) {
                    // No tiles left. Pad message to 4 items.
                    message.append("null ");
                }
            }
        }


        message.append("turn ");
        message.append(playerName);

        for (Player player : players) {
            player.getPeer().sendMessage(message.toString());
        }


    }

    private void sendMoveAnnouncement(String name, Move move, int points) {
        for (Player player : players) {
            player.getPeer().sendMoveMessage(name, move, points);
        }
    }

    private void sendReplaceAnnouncement(String name, Tile removed, Tile drawn) {
        for (Player player : players) {
            player.getPeer().sendReplaceMessage(name, removed, drawn);
        }
    }

    /**
     * Let's all the players know that the given player has to skip.
     *
     * @param playerName The player who has to skip.
     */
    private void sendSkipAnnouncement(String playerName) {
        for (Player player : players) {
            player.getPeer().sendSkipMessage(playerName);
        }
    }

    private void sendLeaderBoardAnnouncement() {
        Map<String, Integer> sortScores = new HashMap<>();

        for (Player player : players) {
            sortScores.put(player.getName(), player.getScore());
        }

        StringBuilder message = new StringBuilder();

        message.append("game finished leaderboard ");

        // Sort the scores.
        while (!sortScores.isEmpty()) {
            int highest = Integer.MIN_VALUE;
            String highestName = "";
            for (Map.Entry<String, Integer> score : sortScores.entrySet()) {
                if (score.getValue() >= highest) {
                    highest = score.getValue();
                    highestName = score.getKey();
                }
            }

            message.append(highestName);
            message.append(" ");
            message.append(highest);
            message.append(" ");

            sortScores.remove(highestName);
        }

        for (Player player : players) {
            player.getPeer().sendMessage(message.toString());
        }
    }

    /**
     * Stops the game because a player disconnected.
     * Informs the players of this development.
     *
     * @param playerName The name of the player who disconnected.
     */
    private void stopGamePlayerDisconnected(String playerName) {
        System.out.println("Connection to client \'" + playerName + "\' lost during a game.");

        for (Player player : players) {
            // We are also sending this message to the one who disconnected.
            // This is not a problem however, as that is handled gracefully.
            player.getPeer().sendPlayerLeftMessage(playerName);
        }

        gameIsNowOver();
    }

    /**
     * Stops the game because there are no moves left.
     * This is the normal way a game stops.
     */
    private void stopGameNoMovesLeft() {
        // Subtract all the tiles left in a players hand from the player's scores.
        for (Player player : players) {
            player.endGameSubtractTilesFromScore();
        }

        sendLeaderBoardAnnouncement();

        gameIsNowOver();
    }

    private void distributeChatMessages() {
        for (Player player : players) {
            String message = player.getPeer().getNextChatMessage();

            while (message != null) {
                for (Player sendPlayer : players) {
                    sendPlayer.getPeer().sendChatMessage(player.getName(), message);
                }

                message = player.getPeer().getNextChatMessage();
            }
        }
    }
}
