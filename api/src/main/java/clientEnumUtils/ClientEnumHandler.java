package clientEnumUtils;

import enums.ClientMessages;

/**
 * Utility class to map string commands from server to corresponding ClientMessages enum values.
 * Helps interpret textual protocol messages from the server.
 * Part of the network Tic Tac Toe client.
 *
 * @version 1.0
 * @created June 2025
 */
public class ClientEnumHandler {

    /**
     * Finds a matching ClientMessages enum constant for the given keyword.
     *
     * @param keyword the string representation of the client command
     * @return the corresponding ClientMessages enum or null if not found
     */
    public static ClientMessages enumFinder(String keyword) {
        for (ClientMessages messageEnum : ClientMessages.values()) {
            if (messageEnum.name().equals(keyword)) {
                return messageEnum;
            }
        }
        return null;
    }
}
