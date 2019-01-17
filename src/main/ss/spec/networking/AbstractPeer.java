package ss.spec.networking;

import ss.spec.Color;
import ss.spec.Tile;

public abstract class AbstractPeer implements Runnable {

    private Connection connection;

    private boolean peerConnected;

    public AbstractPeer(Connection connection) {
        this.connection = connection;

        peerConnected = !connection.isDead();
    }

    public boolean isPeerConnected() {
        return peerConnected;
    }

    /**
     * Watches for messages from the other end of the connection.
     */
    @Override
    public void run() {
        while (isPeerConnected()) {
            try {
                String message = connection.readMessage();
                handleReceivedMessage(message);
            } catch (DeadConnectionException e) {
                // Connection dead.
                // Thread can stop now.
                peerConnected = false;

                // TODO: proper logging.
            }
        }

        // TODO: Nice logging.
        System.out.println("Connection read thread stopping...");
    }

    /**
     * This function get's called by `run` when a new message arrives over the connection.
     */
    abstract protected void handleReceivedMessage(String message);

    /**
     * Sends a message to the peer. Does not fail when not connected,
     * but `isPeerconnected()` will return false afterwards.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) {
        if (peerConnected) {
            try {
                connection.sendMessage(message);
            } catch (DeadConnectionException e) {
                peerConnected = false;
            }
        }
    }

    public void sendInvalidCommandError() {
        sendMessage("invalid command");
    }

    protected String convertTileToProtocol(Tile tile) {
        return "" +
                convertColorToProtocol(tile.getFlatSide()) +
                convertColorToProtocol(tile.getClockwise1()) +
                convertColorToProtocol(tile.getClockwise2()) +
                tile.getPoints();
    }

    private Character convertColorToProtocol(Color color) {
        Character result = null;

        switch (color) {
            case RED:
                result = 'R';
                break;
            case BLUE:
                result = 'B';
                break;
            case GREEN:
                result = 'G';
                break;
            case YELLOW:
                result = 'Y';
                break;
            case PURPLE:
                result = 'P';
                break;
            case WHITE:
                result = 'W';
                break;
        }

        return result;
    }
}
