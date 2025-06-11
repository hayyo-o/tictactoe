package enums;

/**
 * Enum representing messages sent from the server to the client
 * as part of the custom text-based communication protocol.
 */
public enum ServerMessages {

    /**
     * Initial welcome message after a successful connection.
     */
    WELCOME,

    /**
     * Notifies the client that the game has started.
     */
    START,

    /**
     * Informs the client that it's their turn to make a move.
     */
    YOUR_TURN,

    /**
     * Sends the opponent's move to the client.
     */
    MOVE,

    /**
     * Indicates that the client is the winner of the game.
     */
    WINNER,

    /**
     * Indicates that the game has ended in a draw.
     */
    DRAW,

    /**
     * Sent when an error occurs.
     */
    ERROR,

    /**
     * Informs the client that the opponent has disconnected.
     */
    DISCONNECT
}
