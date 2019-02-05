package ss.spec.networking;

public abstract class AbstractPeer implements Runnable {

    public static final String INVALID_COMMAND_ERROR_MESSAGE = "invalidCommand";
    public static final String INVALID_NAME_ERROR_MESSAGE = "invalidName";
    public static final String INVALID_MOVE_ERROR_MESSAGE = "invalidMove";

    private Connection connection;

    private boolean peerConnected;

    private boolean verbose;

    protected AbstractPeer(Connection connection, boolean verbose) {
        this.connection = connection;

        peerConnected = !connection.isDead();

        this.verbose = verbose;
    }

    public boolean isPeerConnected() {
        return peerConnected;
    }

    public boolean verbosePrinting() {
        return verbose;
    }

    public void disconnect() {
        connection.killConnection();
        peerConnected = false;
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
            }
        }
        System.out.println("Peer disconnected...");
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
                if (verbosePrinting()) {
                    System.out.println("Sending: \'" + message + "\'.");
                }
                connection.sendMessage(message);
            } catch (DeadConnectionException e) {
                peerConnected = false;
            }
        }
    }

    public void sendInvalidCommandError(InvalidCommandException e) {
        // Would be nice to send the message included in the exception.
        // But the protocol does not allow for that.
        sendMessage(INVALID_COMMAND_ERROR_MESSAGE);
    }
}
