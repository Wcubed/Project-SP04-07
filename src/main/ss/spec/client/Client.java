package ss.spec.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {

    // TODO: you should be able to enter the port and ip through command line.
    //       Is in the requirements. As with the server, the protocoll states port 4000...
    //       So how to fix that?
    private static final String USAGE = "usage: <name> <address>";

    // TODO: AI. (Optional for single person groups)

    // TODO: Hint functionality.

    // TODO: Write readme.md according to the info in the module manual.

    // TODO: Check that everything is ready for submission. See the module manual for what should
    //       be handed in.

    // TODO: Does there need to be javadoc? and for which classes?


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(USAGE);
            System.exit(0);
        }

        String name = args[0];
        InetAddress addr = null;

        // check args[1] - the IP-address.
        try {
            addr = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            System.out.println(USAGE);
            System.out.println("ERROR: host " + args[1] + " unknown");
            System.exit(0);
        }

        try {
            ClientController controller = new ClientController(name, addr);
        } catch (IOException e) {
            // TODO: Allow user to try connecting again.
            System.out.println("Something went wrong while connecting:");
            e.printStackTrace();
        }

        System.out.println("Server disconnected, exiting.");
    }
}
