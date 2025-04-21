package clientEnumUtils;

import enums.ClientMessages;

public class ClientEnumHandler {
    public static ClientMessages enumFinder(String keyword) {
        for (ClientMessages messageEnum : ClientMessages.values()) {
            if (messageEnum.name().equals(keyword)) {
                return messageEnum;
            }
        }
        return null;
    }
}
