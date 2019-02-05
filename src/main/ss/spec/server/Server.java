package ss.spec.server;

import ss.spec.networking.SocketConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    // TODO: Choose 3 classes where specifications are interesting and relevant: motivate why you
    //  choose these (so not only in the server, but you can also choose from the client.
    //  classes, and document the classes and all their methods with Javadoc, and with JML pre- and
    //   post-
    //  conditions and class invariants (JML specifications must type-check with OpenJML).

    // TODO: mention in raport which classes were JML specified and why you chose those classes.
    //   Client -> GameModel. I think because of the interesting invariants?
    //   AbstractPeer and ClientPeer -> because Game and lobby are self-sufficient black boxes
    //   and are therefore not specifyable in a meaningfull way.
    //   pick one other one.

    // TODO: Extensive documentation should be
    //  provided for the three most complex self-defined classes in the server application.
    //  as determined by the Weighted methods per class metric.
    // According to the metrics that is Lobby, ClientPeer and Game.

    // TODO: There should be a README file with information about installation and starting the
    //  game, indicating for
    //  example which directories and files are necessary, and conditions for the installation.
    //  After reading
    //  this file, a user should be able to install and execute the game without any problem. The
    //   README file
    //   should be located in the root folder of the project.

    // TODO: Compile the javadoc when ready.

    // TODO: include the pre-compiled jar files with the packaged zip.


    // Protocol dictates port 4000.
    private static final int PORT = 4000;

    private final Lobby lobby;

    public Server() {
        lobby = new Lobby();
    }

    public void start() {
        // try to open a server socket;
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("ERROR: could not create a socket on port " + PORT);
            return;
        }

        // Start the lobby thread.
        Thread lobbyThread = new Thread(lobby);
        lobbyThread.start();

        System.out.println("Server up and running.");

        while (true) {
            Socket clientSocket;

            try {
                // Wait for clients to connect.
                clientSocket = serverSocket.accept();

                SocketConnection connection = new SocketConnection(clientSocket);
                ClientPeer newClient = new ClientPeer(connection, true);

                System.out.println("New client connected!");

                Thread newConnectionThread = new Thread(newClient);
                newConnectionThread.start();

                // Hand the client off to the lobby.
                lobby.addNewClient(newClient);

            } catch (IOException e) {
                System.out.println(
                        "Something went wrong while trying to accept a clients connection: \'" +
                                e.getMessage() + "\'.");
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server();

        server.start();
    }
}
