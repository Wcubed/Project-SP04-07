package ss.spec.server;

import ss.spec.gamepieces.Board;
import ss.spec.gamepieces.EmptyTileBagException;
import ss.spec.gamepieces.Tile;
import ss.spec.gamepieces.TileBag;
import ss.spec.networking.ClientPeer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Game implements Runnable {

    private boolean gameOver;

    private ArrayList<ClientPeer> players;
    private Board board;
    private TileBag bag;

    private ArrayList<String> turnOrder;
    private int currentTurnPlayer;

    private HashMap<String, ArrayList<Tile>> playerTiles;

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

        // Set up tile lists.
        for (ClientPeer player : players) {
            playerTiles.put(player.getName(), new ArrayList<>());
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

        // Let everyone know their starting tiles.
        sendTileAnnouncements();
    }


    public void doSingleGameThreadIteration() {
        for (ClientPeer player : players) {
            if (!player.isPeerConnected()) {
                stopGamePlayerDisconnected(player.getName());
                break;
            }

            // TODO: Somewhere in here, check if the tile bag is emtpy.
            //       And no one can make a turn anymore.
            //       Becaus that means the game is over.

            if (player.getName().equals(getCurrentTurnPlayerName())) {
                // It's this player's turn.

                switch (player.getState()) {
                    case GAME_AWAITING_TURN:
                        // TODO: check if the player can actually make a move.
                        player.clientDecideMove();
                        // Notify everyone that this player's turn has started.
                        sendTurnAnnouncement(player.getName());
                        break;
                    case PEER_DECIDE_MOVE:
                        // Waiting for the peer to send a move message.
                        // Do nothing.
                        break;
                    case GAME_VERIFY_MOVE:
                        // TODO: Verify and make the move.
                        // TODO: Notify everyone of the move.

                        // Move is valid, the turn goes to the next player.
                        attemptDrawTileForPlayer(player.getName());
                        sendTileAnnouncements();
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

            // TODO: End the game.

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
     * Let's all the players know who's turn it is.
     */
    private void sendTurnAnnouncement(String playerName) {
        for (ClientPeer player : players) {
            player.sendTurnMessage(playerName);
        }
    }

    /**
     * Let's all the players know who has what tiles.
     */
    private void sendTileAnnouncements() {
        for (ClientPeer player : players) {
            player.sendTileAnnouncement(playerTiles);
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
}
