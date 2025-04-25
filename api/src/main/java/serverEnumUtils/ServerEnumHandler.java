package serverEnumUtils;

import enums.ServerMessages;

public class ServerEnumHandler {
    public static ServerMessages enumFinder(String keyword) {
        for (ServerMessages messageEnum : ServerMessages.values()) {
            if (messageEnum.name().equals(keyword)) {
                return messageEnum;
            }
        }
        return null;
    }
}
