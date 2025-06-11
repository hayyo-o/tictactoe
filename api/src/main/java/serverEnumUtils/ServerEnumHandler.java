package serverEnumUtils;

import enums.ServerMessages;

/**
 * Utility for converting incoming protocol keywords into {@link ServerMessages} enums.
 */
public class ServerEnumHandler {

    /**
     * Converts a raw protocol string into its corresponding {@link ServerMessages} enum constant.
     *
     * @param keyword the exact name of the server message (e.g. "WELCOME", "MOVE", etc.)
     * @return the matching {@link ServerMessages} constant, or {@code null} if no match is found
     */
    public static ServerMessages enumFinder(String keyword) {
        for (ServerMessages messageEnum : ServerMessages.values()) {
            if (messageEnum.name().equals(keyword)) {
                return messageEnum;
            }
        }
        return null;
    }
}
