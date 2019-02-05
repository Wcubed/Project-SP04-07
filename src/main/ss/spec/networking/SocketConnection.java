package ss.spec.networking;

import java.io.*;
import java.net.Socket;

public class SocketConnection implements Connection {

    private final Socket socket;

    private BufferedReader in;
    private BufferedWriter out;

    private boolean connectionDead;

    public SocketConnection(Socket socket) {
        this.socket = socket;

        if (socket.isConnected()) {
            connectionDead = false;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                connectionDead = true;
            }
        } else {
            connectionDead = true;
        }
    }

    /**
     * Returns whether the connection is dead or not.
     * Getting `false` from this method might still mean that the connection is dead.
     * We just haven't noticed yet.
     *
     * @return True when the connection is guaranteed to be dead.
     */
    @Override
    public boolean isDead() {
        return connectionDead;
    }

    /**
     * Call when you want to end the communications.
     * Can be called multiple times without issue.
     */
    @Override
    public void killConnection() {
        if (!isDead()) {
            connectionDead = true;

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
    @Override
    public void sendMessage(String message) throws DeadConnectionException {
        if (isDead()) {
            throw new DeadConnectionException();
        }
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            // Going to assume the connection is dead.
            killConnection();
            throw new DeadConnectionException();
        }
    }

    @Override
    public String readMessage() throws DeadConnectionException {
        if (isDead()) {
            throw new DeadConnectionException();
        }

        String message;

        try {
            message = in.readLine();

            if (message == null) {
                // Dead connection.
                killConnection();
                throw new DeadConnectionException();
            }
        } catch (IOException e) {
            // Going to assume the connection is dead.
            killConnection();
            throw new DeadConnectionException();
        }

        return message;
    }
}
