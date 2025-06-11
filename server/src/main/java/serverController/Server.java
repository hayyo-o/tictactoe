package serverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central server class for managing client connections, matchmaking, and active games.
 * <p>
 * Starts the Acceptor and MatchMaker threads, maintains the waiting queue and active GameManager instances,
 * and handles graceful shutdown on user request.
 * </p>
 *
 * @version 1.0
 * @created April 2025
 */
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final Object queueLock = new Object();
    private final Set<Connection> queue;
    private final Set<GameManager> gameManagerSet;

    private final Acceptor acceptor;
    private final MatchMaker mm;

    /**
     * Application entry point. Starts the server and waits for the user to press Enter to terminate.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        log.info("Server started");

        Server server = new Server();

        System.out.println("Server started, press Enter to stop");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        log.info("Server is terminated");
        server.terminate();
    }

    /**
     * Constructs the Server, initializes the waiting queue and game set,
     * and launches the Acceptor and MatchMaker threads.
     */
    public Server() {
        queue = new HashSet<>();
        gameManagerSet = new HashSet<>();

        acceptor = new Acceptor(this, 8080);
        mm = new MatchMaker(this);
    }

    /**
     * Adds a new GameManager to the active games set.
     *
     * @param gameManager the GameManager instance to add
     */
    public synchronized void addGame(GameManager gameManager) {
        gameManagerSet.add(gameManager);
    }

    /**
     * Removes a GameManager from the active games set.
     *
     * @param gameManager the GameManager instance to remove
     */
    public synchronized void removeGame(GameManager gameManager) {
        gameManagerSet.remove(gameManager);
    }

    /**
     * Cleans up completed games by removing any GameManager
     * whose game is no longer running and clearing references.
     */
    public synchronized void cleanupGames() {
        Iterator<GameManager> iterator = gameManagerSet.iterator();
        while (iterator.hasNext()) {
            GameManager gameManager = iterator.next();
            if (!gameManager.isGameRunning()) {
                for (Connection conn : gameManager.getConnections()) {
                    conn.setGameManager(null);
                }
                iterator.remove();
            }
        }
    }

    /**
     * Terminates the server by stopping the Acceptor and MatchMaker,
     * closing all queued connections and active games.
     */
    public void terminate() {
        acceptor.terminate();
        mm.terminate();
        synchronized (queue) {
            for (Connection connection : new HashSet<>(queue)) {
                connection.terminate();
            }
        }
        synchronized (gameManagerSet) {
            for (GameManager gameManager : new HashSet<>(gameManagerSet)) {
                gameManager.terminate();
            }
        }
    }

    /**
     * Creates a new Connection for a raw socket and starts its thread.
     *
     * @param clientSocket the accepted client socket
     */
    public void addConnection(Socket clientSocket) {
        log.info("Creating new connection with client");
        Connection connection = new Connection(this, clientSocket);
        Thread connectionThread = new Thread(connection);
        connectionThread.start();
    }

    /**
     * Adds an authenticated Connection to the matchmaking queue.
     *
     * @param connection the authenticated Connection to add
     */
    public void addAuthenticatedConnection(Connection connection) {
        synchronized (queue) {
            queue.add(connection);
        }
    }

    /**
     * Removes a Connection from the matchmaking queue.
     *
     * @param connection the Connection to remove
     */
    public void removeConnection(Connection connection) {
        synchronized (queue) {
            queue.remove(connection);
        }
    }

    /**
     * Re-adds an existing Connection to the matchmaking queue (e.g., after a disconnect).
     *
     * @param connection the Connection to re-add
     */
    public void addExistingConnection(Connection connection) {
        log.info("Adding existing player {} to queue", connection.getName());
        synchronized (queue) {
            queue.add(connection);
        }
    }

    /**
     * Retrieves the set of currently waiting, authenticated clients.
     *
     * @return a Set of ready Connection objects
     */
    public Set<Connection> getQueue() {
        synchronized (queueLock) {
            return queue.stream()
                    .filter(Connection::getReady)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Checks if a given username is already in use by any queued or active player.
     *
     * @param name the username to check
     * @return true if the name is already taken; false otherwise
     */
    public boolean nameExists(String name) {
        if (name == null) {
            return false;
        }

        Set<String> names = new HashSet<>();
        synchronized (queueLock) {
            for (Connection connection : queue) {
                String connectionName = connection.getName();
                if (connectionName != null) {  // Only add non-null names
                    names.add(connectionName);
                }
            }
        }
        synchronized (gameManagerSet) {
            for (GameManager gameManager : gameManagerSet) {
                gameManager.getConnections()
                        .forEach(connection -> {
                            String connectionName = connection.getName();
                            if (connectionName != null) {
                                names.add(connectionName);
                            }
                        });
            }
        }
        return names.contains(name);
    }
}
