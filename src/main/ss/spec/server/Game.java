package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.EmptyTileBagException;
import ss.spec.gamepieces.Tile;
import ss.spec.gamepieces.TileBag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Game implements Runnable {

    private boolean gameOver;

    private ArrayList<ClientPeer> players;
    private Board board;
    private TileBag bag;

    private ArrayList<String> turnOrder;
    private int currentTurnPlayer;

    // TODO: Do we want to change these two pieces of info to a single separate class?
    private HashMap<String, ArrayList<Tile>> playerTiles;
    private HashMap<String, Integer> playerScores;

    /**
     * Instantiates the Game class with the given players, board and TileBag.
     *
     * @param players The participating players, order doesn't matter.
     * @param board   The board to play on.
     * @param bag     The tile bag to use.
     */
    public Game(List<ClientPeer> players, Board board, TileBag bag) {
        this.players = new ArrayList<>(players);
        this.board = board;
        this.bag = bag;

        gameOver = false;

        this.turnOrder = new ArrayList<>();
        this.playerTiles = new HashMap<>();
        this.playerScores = new HashMap<>();
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
    public List<ClientPeer> getPlayers() {
        return players;
    }

    @Override
    public void run() {
        setUpGame();

        while (!isGameOver()) {
            doSingleGameThreadIteration();

            // TODO: Tweak sleeping for final application, maybe even remove it?
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // TODO: cleanup?
    }

    /**
     * Does everything needed to get ready for a game.
     * Initializes the tile bag
     * Determines the turn order.
     * Makes sure everyone has 4 tiles.
     */
    public void setUpGame() {
        bag.addAllStartingTiles();

        // Set up tile and score lists.
        for (ClientPeer player : players) {
            playerTiles.put(player.getName(), new ArrayList<>());
            playerScores.put(player.getName(), 0);
        }

        decideTurnOrder();

        // Fill the client's tiles till everyone has 4.
        for (ClientPeer player : players) {
            while (playerTiles.get(player.getName()).size() < 4) {
                attemptDrawTileForPlayer(player.getName());
            }
        }

        // Let the clients know the game is on.
        for (ClientPeer player : players) {
            player.sendStartMessage(turnOrder);
            player.sendOrderMessage(turnOrder);

            player.awaitTurn();
        }
    }


    public void doSingleGameThreadIteration() {
        for (ClientPeer player : players) {
            if (!player.isPeerConnected()) {
                stopGamePlayerDisconnected(player.getName());
                break;
            }


            if (player.getName().equals(getCurrentTurnPlayerName())) {
                // It's this player's turn.

                switch (player.getState()) {
                    case GAME_AWAITING_TURN:
                        if (board.hasValidMoves(playerTiles.get(player.getName()))) {
                            player.clientDecideMove();
                            // Notify everyone that this player's turn has started.
                            sendTileAndTurnAnnouncement(player.getName());
                        } else {
                            // Whoops, no move for this player.
                            player.decideSkip();
                            // Announce that this player does not have a valid move option.
                            sendSkipAnnouncement(player.getName());
                        }
                        break;
                    case PEER_DECIDE_MOVE:
                        // Waiting for the peer to send a move message.
                        // Do nothing.
                        break;
                    case GAME_VERIFY_MOVE:
                        // TODO: Verify and make the move.
                        // TODO: update the score of the player.
                        // TODO: Notify everyone of the move.

                        // Move is valid, the turn goes to the next player.
                        attemptDrawTileForPlayer(player.getName());
                        player.awaitTurn();
                        advanceTurnPlayer();
                        break;
                    case PEER_DECIDE_SKIP:
                        // Peer is deciding whether to skip or not.
                        // Do nothing.
                        break;
                    case GAME_VERIFY_SKIP:
                        // TODO: Check if the player wants to replace a tile.
                        // TODO: announce the skip.

                        // Skip is valid, the turn goes to the next player.
                        // TODO: Do we announce tiles here? Or is that not needed.
                        player.awaitTurn();
                        advanceTurnPlayer();
                        break;
                    default:
                        // This is a weird state to be in. Shouldn't happen.
                        // Awaiting turn is a save state, so put them in that.
                        // TODO: logging.
                        player.awaitTurn();
                }
            }

            // Check if the game is over.
            if (bag.getNumTilesLeft() == 0) {
                // No tiles left in the bag.
                // Are there any players who can make a move?
                boolean noOneCanMove = true;
                for (ArrayList<Tile> tiles : playerTiles.values()) {
                    if (board.hasValidMoves(tiles)) {
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
    }

    private void decideTurnOrder() {
        // TODO: Properly decide turn order.
        for (ClientPeer player : players) {
            turnOrder.add(player.getName());
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
     * Will not draw a tile for a non-existing player.
     * Will not allow a player to have more than 4 tiles.
     * Will not crash on an empty bag.
     * All of the cases above are handled gracefully.
     *
     * @param playerName The name of the player to draw a tile for.
     */
    private void attemptDrawTileForPlayer(String playerName) {
        if (playerTiles.containsKey(playerName)) {
            ArrayList<Tile> tiles = playerTiles.get(playerName);

            if (tiles.size() < 4) {
                try {
                    tiles.add(bag.takeTile());
                } catch (EmptyTileBagException e) {
                    // Empty bag.
                    // No need to do anything.
                }
            }
        }
    }

    /**
     * Let's all the players know who has what tiles, and who's turn it is.
     *
     * @param playerName The player who's turn it is.
     */
    private void sendTileAndTurnAnnouncement(String playerName) {
        for (ClientPeer player : players) {
            player.sendTileAndTurnAnnouncement(playerTiles, playerName);
        }
    }

    /**
     * Let's all the players know that the given player has to skip.
     *
     * @param playerName The player who has to skip.
     */
    private void sendSkipAnnouncement(String playerName) {
        for (ClientPeer player : players) {
            player.sendSkipMessage(playerName);
        }
    }

    /**
     * Stops the game because a player disconnected.
     * Informs the players of this development.
     *
     * @param playerName The name of the player who disconnected.
     */
    private void stopGamePlayerDisconnected(String playerName) {
        for (ClientPeer player : players) {
            // We are also sending this message to the one who disconnected.
            // This is not a problem however, as that is handled gracefully.
            player.sendPlayerLeftMessage(playerName);
        }

        gameIsNowOver();
    }

    /**
     * Stops the game because there are no moves left.
     * This is the normal way a game stops.
     */
    private void stopGameNoMovesLeft() {
        // Subtract all the tiles left in a players hand from the player's scores.
        for (Map.Entry<String, ArrayList<Tile>> tiles : playerTiles.entrySet()) {
            for (Tile tile : tiles.getValue()) {
                // TODO: Maybe we DO need to change the tiles and points into a proper class.
                playerScores.put(tiles.getKey(),
                        playerScores.get(tiles.getKey()) - tile.getPoints());
            }
        }

        for (ClientPeer player : players) {
            player.sendLeaderBoardMessage(playerScores);
        }

        gameIsNowOver();
    }
}
