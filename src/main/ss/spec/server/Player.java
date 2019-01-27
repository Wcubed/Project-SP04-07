package ss.spec.server;

import ss.spec.Player.AbstractPlayer;

public class Player extends AbstractPlayer {

    private ClientPeer peer;

    public Player(ClientPeer peer) {
        super();

        this.peer = peer;
    }

    // ---------------------------------------------------------------------------------------------

    public ClientPeer getPeer() {
        return peer;
    }


    // ---------------------------------------------------------------------------------------------

    public String getName() {
        return peer.getName();
    }

    public boolean isPeerConnected() {
        return peer.isPeerConnected();
    }

    // ---------------------------------------------------------------------------------------------


}
