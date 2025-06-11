package clientEnumUtils;

import enums.ClientMessages;

/**
 * Utility class for constructing protocol-compliant text commands
 * that the client sends to the server.
 * <p>
 * Part of the network Tic Tac Toe client.
 *
 * @version 1.0
 * @created June 2025
 */
public class ClientMessageBuilder {

    /**
     * Constructs a HELLO message with the given username.
     *
     * @param username the player's chosen username
     * @return formatted HELLO command
     */
    public static String hello(String username) {
        return ClientMessages.HELLO + " " + username;
    }

    /**
     * Constructs a MOVE command with grid coordinates.
     *
     * @param x the row index
     * @param y the column index
     * @return formatted MOVE command
     */
    public static String move(int x, int y) {
        return ClientMessages.MOVE + " " + x + " " + y;
    }

    /**
     * Constructs an OK message indicating readiness.
     *
     * @return OK command
     */
    public static String ok() {
        return ClientMessages.OK.toString();
    }

    /**
     * Constructs a QUIT message indicating player is leaving.
     *
     * @return QUIT command
     */
    public static String quit() {
        return ClientMessages.QUIT.toString();
    }
}
