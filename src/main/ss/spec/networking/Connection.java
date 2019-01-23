package ss.spec.networking;

public interface Connection {

    /**
     * Returns whether the connection is dead or not.
     * Getting `false` from this method might still mean that the connection is dead.
     * We just haven't noticed yet.
     *
     * @return True when the connection is guaranteed to be dead.
     */
    boolean isDead();

    /**
     * Call when you want to end the communications.
     * Can be called multiple times without issue.
     */
    void killConnection();

    /**
     * Sends a message over the connection.
     * If you get a `DeadConnectionException` that means the connection has already been closed.
     *
     * @param message The message to send.
     */
    void sendMessage(String message) throws DeadConnectionException;

    /**
     * Reads a message from the connection. Is blocking.
     *
     * @return The message that was read.
     */
    String readMessage() throws DeadConnectionException;
}
