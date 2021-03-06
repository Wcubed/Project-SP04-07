package ss.spec.server;

import ss.spec.gamepieces.AbstractPlayer;

public class Player extends AbstractPlayer {

    private final ClientPeer peer;

    public Player(ClientPeer peer) {
        super(peer.getName());

        this.peer = peer;
    }

    // ---------------------------------------------------------------------------------------------

    public ClientPeer getPeer() {
        return peer;
    }


    // ---------------------------------------------------------------------------------------------

    public boolean isPeerConnected() {
        return peer.isPeerConnected();
    }

    // ---------------------------------------------------------------------------------------------


}
