package serverEnumUtils;

import enums.ServerMessages;

public class ServerMessageBuilder {
    public static String welcome (String player1) {
        return ServerMessages.WELCOME.toString() + " " + player1;
    }
    public static String start(String player1, String player2) {
        return ServerMessages.START.toString() + " " + player1 + " " + player2;
    }

    public static String turn(String player1) {
        return ServerMessages.YOUR_TURN.toString() + " " + player1;
    }
    public static String move(String player1, int x, int y) {
        return ServerMessages.MOVE.toString() + " " + player1 + " " + x + " " + y;
    }
    public static String winner(String player1) {
        return ServerMessages.WINNER.toString() + " " + player1;
    }
    public static String draw() {
        return ServerMessages.DRAW.toString();
    }
    public static String error(String message) {
        return ServerMessages.ERROR.toString() + " " + message;
    }
    public static String disconnect() {
        return ServerMessages.DISCONNECT.toString();
    }
}
