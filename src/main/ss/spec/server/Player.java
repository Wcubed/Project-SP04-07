package ss.spec.server;

import ss.spec.Player.AbstractPlayer;

public class Player extends AbstractPlayer {

    private ClientPeer peer;

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
