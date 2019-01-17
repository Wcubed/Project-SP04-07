package ss.spec.networking;

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
                parseMessage(message);
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
    abstract protected void parseMessage(String message) throws DeadConnectionException;

    /**
     * Sends a message to the peer. Throws a DeadConnectionException when the peer is not connected.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) throws DeadConnectionException {
        if (peerConnected) {
            try {
                connection.sendMessage(message);
            } catch (DeadConnectionException e) {
                peerConnected = false;
                throw new DeadConnectionException();
            }
        } else {
            throw new DeadConnectionException();
        }
    }

    public void sendInvalidCommandError() throws DeadConnectionException {
        sendMessage("invalid command");
    }
}
