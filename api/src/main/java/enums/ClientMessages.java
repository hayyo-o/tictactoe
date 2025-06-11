package enums;

/**
 * Enum representing messages sent from the client to the server as part of the custom text-based
 * communication protocol.
 */
public enum ClientMessages {

  /** Initial greeting message sent when connecting to the server. */
  HELLO,

  /** Acknowledgment message confirming receipt or agreement (e.g., start game). */
  OK,

  /** Sent by the client to indicate their move during the game. */
  MOVE,

  /** Indicates that the client wants to quit the game or disconnect. */
  QUIT
}
