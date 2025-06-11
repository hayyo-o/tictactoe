package serverEnumUtils;

import enums.ServerMessages;

/**
 * Builder for formatting outgoing server
 * messages according to the text‐based protocol.
 * Each method prepends the appropriate
 * {@link ServerMessages} keyword and appends any parameters.
 */
public class ServerMessageBuilder {

    /**
     * Builds the initial welcome message, including the player's name.
     *
     * @param player1 the name of the player receiving the welcome
     * @return a protocol string like "WELCOME Alice"
     */
    public static String welcome(String player1) {
        return ServerMessages.WELCOME.toString() + " " + player1;
    }

    /**
     * Builds the game start notification, listing both players.
     *
     * @param player1 the first player's name
     * @param player2 the second player's name
     * @return a protocol string like "START Alice Bob"
     */
    public static String start(String player1, String player2) {
        return ServerMessages.START.toString() + " " + player1 + " " + player2;
    }

    /**
     * Informs a client that it is now their turn.
     *
     * @param player1 the name of the player whose turn it is
     * @return a protocol string like "YOUR_TURN Alice"
     */
    public static String turn(String player1) {
        return ServerMessages.YOUR_TURN.toString() + " " + player1;
    }

    /**
     * Formats a move made by a player.
     *
     * @param player1 the name of the player making the move
     * @param x        the row index of the move (0-based)
     * @param y        the column index of the move (0-based)
     * @return a protocol string like "MOVE Alice 1 2"
     */
    public static String move(String player1, int x, int y) {
        return ServerMessages.MOVE.toString() + " " + player1 + " " + x + " " + y;
    }

    /**
     * Notifies the client that they have won the game.
     *
     * @param player1 the name of the winning player
     * @return a protocol string like "WINNER Alice"
     */
    public static String winner(String player1) {
        return ServerMessages.WINNER.toString() + " " + player1;
    }

    /**
     * Notifies the client that the game ended in a draw.
     *
     * @return the protocol string "DRAW"
     */
    public static String draw() {
        return ServerMessages.DRAW.toString();
    }

    /**
     * Sends an error message to the client.
     *
     * @param message descriptive error detail (e.g. "Invalid move")
     * @return a protocol string like "ERROR Invalid move"
     */
    public static String error(String message) {
        return ServerMessages.ERROR.toString() + " " + message;
    }

    /**
     * Informs the client that the opponent has disconnected.
     *
     * @return the protocol string "DISCONNECT"
     */
    public static String disconnect() {
        return ServerMessages.DISCONNECT.toString();
    }
}
