package ss.spec.networking;

/**
 * Enum to denote the current state the client is in.
 * The first word indicates who we are waiting for to further the state.
 * PEER: means that we are waiting for a message from the peer.
 * It is the responsibility of the peer thread to further the state.
 * LOBBY: means we are waiting for an action or verification from the lobby.
 * It is the responsibility of the lobby thread to further the state.
 */
public enum ClientState {
    PEER_CONNECT_MESSAGE,
    LOBBY_NAME_VERIFICATION,
    PEER_GAME_REQUEST,
}
