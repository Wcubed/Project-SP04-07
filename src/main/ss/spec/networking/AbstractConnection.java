package ss.spec.networking;

import java.io.*;
import java.net.Socket;


public abstract class AbstractConnection implements Runnable {

    private Socket socket;

    private BufferedReader in;
    private BufferedWriter out;

    private boolean connectionDead;

    public AbstractConnection(Socket socket) throws IOException {
        this.socket = socket;

        if (socket.isConnected()) {
            connectionDead = false;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
    }

    /**
     * Watches for messages from the other end of the connection.
     */
    @Override
    public void run() {
        while (!isConnectionDead()) {
            try {
                String message = in.readLine();

                if (message == null) {
                    // Dead connection.
                    killConnection();
                } else {
                    handleMessage(message);
                }
            } catch (IOException | DeadConnectionException e) {
                // TODO: Do we need this stack trace, or can we simply assume that when the read
                //  fails, the connection is dead?
                // e.printStackTrace();

                // Going to assume the connection is dead.
                // TODO: Is this assumption correct?
                killConnection();
            }
        }

        // TODO: Nice logging.
        System.out.println("Connection read thread stopping...");
    }

    /**
     * Returns whether the connection is dead or not.
     * Getting `false` from this method might still mean that the connection is dead.
     * We just haven't noticed yet.
     *
     * @return True when the connection is guaranteed to be dead.
     */
    public boolean isConnectionDead() {
        return connectionDead;
    }

    /**
     * Call when you want to end the communications.
     * Can be called multiple times without issue.
     */
    private void killConnection() {
        if (!isConnectionDead()) {
            connectionDead = true;

            // TODO: Propper logging.
            System.out.println("Killing connection.");

            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends a message over the connection.
     * If you get a `DeadConnectionException` that means the connection can be deleted safely.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) throws DeadConnectionException {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            // TODO: Do we need this stack trace, or can we simply assume that when the write fails,
            //  the connection is dead?
            // e.printStackTrace();

            // Going to assume the connection is dead.
            // TODO: Is this assumption correct?
            killConnection();
            throw new DeadConnectionException();
        }
    }

    /**
     * This function get's called by `run` when a new message arrives over the connection.
     */
    abstract public void handleMessage(String message) throws DeadConnectionException;


    protected void sendInvalidCommandError() throws DeadConnectionException {
        sendMessage("invalid command");
    }
}
