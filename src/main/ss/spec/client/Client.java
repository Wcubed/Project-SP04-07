package ss.spec.client;

import ss.spec.networking.Connection;
import ss.spec.networking.DeadConnectionException;
import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    // Protocol dictates port 4000.
    private static final int PORT = 4000;


    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        InetAddress addr = null;
        Connection connection = null;

        while (addr == null) {
            System.out.println("To which host do you want to connect?");
            System.out.print("> ");
            String addrString = in.nextLine();

            try {
                addr = InetAddress.getByName(addrString);
            } catch (UnknownHostException e) {
                System.out.println("Sorry, host \'" + addrString + "\' is unknown");
                // Try again.
                continue;
            }


            try {
                Socket socket = new Socket(addr, PORT);
                connection = new SocketConnection(socket);
            } catch (IOException e) {
                System.out.println("Something went wrong while trying to connect to \'"
                        + addrString + "\'.");
                // Clear out the address and try again.
                addr = null;
            }
        }

        System.out.println("Connection established.");

        String confirmedName = null;
        boolean chatSupported = false;


        System.out.println("What is your name?");

        while (confirmedName == null) {
            System.out.print("> ");

            String potentialName = in.nextLine();

            if (potentialName.contains(" ")) {
                System.out.println("Sorry, names are not allowed to have spaces.");
                // Try again.
                continue;
            }

            try {
                // We support the chat extension.
                connection.sendMessage("connect " + potentialName + " chat");

                String message = connection.readMessage();

                if (message.equals(ServerPeer.INVALID_NAME_ERROR_MESSAGE)) {
                    System.out.println("Sorry, that name is already taken.");
                    // Try again.
                } else if (message.contains("welcome")) {
                    // Name has been confirmed.
                    confirmedName = potentialName;
                    System.out.println("Welcome " + confirmedName + "!");

                    if (message.contains("chat")) {
                        System.out.println("Server supports chat extension.");
                        chatSupported = true;
                    }
                } else {
                    System.out.println("Something went wrong while communicating with the server.");
                }

            } catch (DeadConnectionException e) {
                System.out.println("The server disconnected.");
                System.exit(0);
            }
        }


        ClientController controller = new ClientController(
                confirmedName, connection, chatSupported);

        controller.startViewThread();

        // This thread will run the network connection.
        controller.runPeer();

        System.out.println("Closing program.");

        // If we fall through, that means the network connection is lost.
        // Make sure the other threads are stopped.
        controller.exitProgram();

        // TUI threads might not want to close, so we use system.exit to force them to stop.
        System.exit(0);
    }
}
