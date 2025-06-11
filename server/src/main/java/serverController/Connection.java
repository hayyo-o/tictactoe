package serverController;

import clientEnumUtils.ClientEnumHandler;
import enums.ClientMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serverEnumUtils.ServerMessageBuilder;

import java.io.*;
import java.net.Socket;

/**
 * Manages a single client connection: reads incoming commands,
 * invokes appropriate server logic, and sends responses.
 *
 * <p>Handles login (HELLO), moves, readiness (OK), and quit (QUIT)
 * according to the custom protocol.</p>
 *
 * @version 1.0
 * @created April 2025
 */
public class Connection implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    private final Server server;
    private final Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private String username = null;
    private boolean ready = false;
    private GameManager gameManager;

    private volatile boolean keepAlive = true;
    private final Object lock = new Object();

    /**
     * Creates a new Connection for the given socket and server.
     * Initializes text‐based I/O streams.
     *
     * @param server the server managing this connection
     * @param socket the client socket to communicate over
     */
    public Connection(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            log.info("Creating IOStreams (text-based)");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true); // auto-flush
        } catch (IOException e) {
            log.error("Failed to create IOStreams", e);
            try {
                socket.close();
            } catch (IOException ex) {
                log.error("Failed to close socket", ex);
            }
        }
    }

    /**
     * Sends a raw protocol message to the client.
     *
     * @param message the formatted message string
     */
    public void sendMessage(String message) {
        log.info("Sending message {}", message);
        out.println(message);
    }

    /**
     * Main loop: reads lines from the client, parses commands,
     * and dispatches login, move, quit, etc.
     */
    @Override
    public void run() {
        log.info("Connection to {}:{}", socket.getInetAddress().getHostAddress(), socket.getPort());
        try {
            String incomingMessage;
            while (keepAlive && (incomingMessage = in.readLine()) != null) {
                log.info("Received message: {}", incomingMessage);
                String[] command = incomingMessage.split(" ");
                ClientMessages keyword = ClientEnumHandler.enumFinder(command[0]);

                if (keyword == ClientMessages.HELLO) {
                    username = command[1];
                    if(server.nameExists(username)) {
                        log.info("User {} already exists", username);
                        sendMessage(ServerMessageBuilder.error("Invalid username"));
                        continue;
                    }
                    log.info("User {} logged in", username);
                    sendMessage(ServerMessageBuilder.welcome(username));
                    ready = true;
                    server.addAuthenticatedConnection(this);
                } else if (keyword == ClientMessages.QUIT) {
                    log.info("User {} sent QUIT", username);
                    ready = false;
                    keepAlive = false;

                    sendMessage(ServerMessageBuilder.disconnect());

                    if (gameManager != null) {
                        gameManager.quit(this);
                    } else {
                        server.removeConnection(this);
                    }

                    // Free username for new connections
                    username = null;

                    return;
                } else if (keyword == null) {
                    log.error("Incorrect incoming message");
                    sendMessage(ServerMessageBuilder.error("InvalidCommand"));

                } else if (keyword == ClientMessages.OK) {
                    log.info("User {} sent OK", username);
                    gameManager.playerReady(this);

                } else if (keyword == ClientMessages.MOVE) {
                    if (gameManager == null) {
                        sendMessage(ServerMessageBuilder.error("Game not active"));
                        continue;
                    }
                    if (command.length < 3) {
                        sendMessage(ServerMessageBuilder.error("InvalidMove"));
                        continue;
                    }
                    int x = Integer.parseInt(command[1]);
                    int y = Integer.parseInt(command[2]);
                    log.debug("Move x=" +x + " y="+y);
                    gameManager.playerMove(this, x, y);

                } else {
                    log.warn("Unknown command received: {}", command[0]);
                    sendMessage(ServerMessageBuilder.error("InvalidCommand"));
                }
            }

        } catch (IOException e) {
            log.error("IOException during client communication", e);
        } finally {
            terminate();
        }
    }

    /**
     * Stops reading new messages.
     */
    public void close() {
        synchronized (lock) {
            keepAlive = false;
        }
    }

    /**
     * Cleans up I/O and socket resources.
     */
    public void terminate() {
        close();
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            log.error("Error closing connection resources", e);
        }
    }

    /**
     * Assigns this connection to a GameManager when a match is found.
     *
     * @param gameManager the game manager for a specific match
     */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Resets state so this connection can be reused for another game.
     */
    public void resetForNewGame() {
        this.gameManager = null;
    }

    /**
     * @return the username associated with this connection
     */
    public String getName() {
        return username;
    }

    /**
     * @return whether this client has completed login (HELLO) and is queued
     */
    public boolean getReady() {
        return ready;
    }

    /**
     * @return the current GameManager this connection is part of, or null
     */
    public GameManager getGameManager() {
        return gameManager;
    }
}
