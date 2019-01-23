package ss.test.networking;

import ss.spec.networking.Connection;
import ss.spec.networking.DeadConnectionException;

import java.util.LinkedList;

public class MockConnection implements Connection {

    private boolean connectionDead;
    private LinkedList<String> testMessages;
    private LinkedList<String> sentMessages;

    public MockConnection() {
        connectionDead = false;

        testMessages = new LinkedList<>();
        sentMessages = new LinkedList<>();
    }

    public void setIsDead(boolean isDead) {
        connectionDead = isDead;
    }

    /**
     * Adds a message to the test message queue, so that it will be read by `readMessage`.
     *
     * @param message The message to add.
     */
    public void addTestMessage(String message) {
        testMessages.add(message);
    }


    public String readSentMessage() {
        return sentMessages.poll();
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean isDead() {
        return connectionDead;
    }

    @Override
    public void killConnection() {
        connectionDead = true;
    }

    @Override
    public void sendMessage(String message) throws DeadConnectionException {
        if (isDead()) {
            throw new DeadConnectionException();
        }

        sentMessages.add(message);
    }

    @Override
    public String readMessage() throws DeadConnectionException {
        if (isDead()) {
            throw new DeadConnectionException();
        }

        String message = testMessages.poll();

        while (message == null) {
            try {
                // Pretend that we are blocking.
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return message;
    }
}
