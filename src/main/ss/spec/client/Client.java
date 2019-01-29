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

    private static final int PORT = 4000;

    // TODO: The following requirements are optional for lone wolfs:
    //       Support for computer players.
    //       Metrics report.
    //       Coverage test for complex server classes.
    //       Telnet test.

    // TODO: you should be able to enter the port and ip through command line.
    //       Is in the requirements. As with the server, the protocoll states port 4000...
    //       So how to fix that?

    // TODO: AI. (Optional for single person groups)

    // TODO: Hint functionality.

    // TODO: Write readme.md according to the info in the module manual.

    // TODO: Check that everything is ready for submission. See the module manual for what should
    //       be handed in.

    // TODO: Does there need to be javadoc? and for which classes?

    // TODO: Implement chat extension? The system is designed to easily allow it.


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
                // TODO: Add supported extensions, if any?
                connection.sendMessage("connect " + potentialName);

                String message = connection.readMessage();

                if (message.equals(ServerPeer.INVALID_NAME_ERROR_MESSAGE)) {
                    System.out.println("Sorry, that name is already taken.");
                    // Try again.
                    continue;
                } else if (message.contains("welcome")) {
                    // TODO: parse extensions, if any.
                    // Name has been confirmed.
                    confirmedName = potentialName;
                    System.out.println("Welcome " + confirmedName + "!");
                } else {
                    System.out.println("Something went wrong while communicating with the server.");
                }

            } catch (DeadConnectionException e) {
                System.out.println("The server disconnected.");
                System.exit(0);
            }
        }


        ClientController controller = new ClientController(confirmedName, connection);

        System.out.println("Closing program.");
    }
}
