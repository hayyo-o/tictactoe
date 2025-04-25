package clientEnumUtils;

import enums.ClientMessages;

public class ClientMessageBuilder {

    public static String hello(String username) {
        return ClientMessages.HELLO.toString() + " " + username;
    }

    public static String move(int x, int y) {
        return ClientMessages.MOVE.toString() + " " + x + " " + y;
    }

    public static String ok() {
        return ClientMessages.OK.toString();
    }

    public static String quit() {
        return ClientMessages.QUIT.toString();
    }
}
