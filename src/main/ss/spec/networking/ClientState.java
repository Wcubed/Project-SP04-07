package ss.spec.networking;

/**
 * Enum to denote the current state the client is in.
 * The first word indicates who we are waiting for to further the state.
 * Other threads can send messages, they just shouldn't touch the state,
 * or do something that would be invalid in this state.
 * <p>
 * PEER: means that we are waiting for a message from the peer.
 * It is the responsibility of the peer thread to further the state.
 * LOBBY: means we are waiting for an action or verification from the lobby.
 * It is the responsibility of the lobby thread to further the state.
 * GAME: means we are waiting for an action from the game thread.
 * </p>
 */
public enum ClientState {
    PEER_AWAITING_CONNECT_MESSAGE,
    LOBBY_VERIFY_NAME,
    PEER_AWAITING_GAME_REQUEST,
    // Client has requested a game, but has not been sent a `waiting` message yet.
    LOBBY_START_WAITING_FOR_PLAYERS,
    LOBBY_WAITING_FOR_PLAYERS,
    
    GAME_AWAITING_TURN,
    PEER_DECIDE_MOVE,
    GAME_VERIFY_MOVE,
    PEER_DECIDE_SKIP,
    GAME_VERIFY_SKIP,

}
