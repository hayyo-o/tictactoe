package serverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Continuously pairs available clients into new games.
 * <p>
 * Monitors the server's waiting queue and, every 500ms,
 * matches clients in pairs, removes them from the queue,
 * creates a GameManager for each pair, and starts it.
 * </p>
 *
 * <p>Runs in its own thread until terminated.</p>
 *
 * @version 1.0
 * @created April 2025
 */
public class MatchMaker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MatchMaker.class);

    private Server server;
    private volatile boolean keepAlive;
    private Thread thread;

    /**
     * Constructs and starts the MatchMaker for the given server.
     *
     * @param server the Server instance whose queue is monitored
     */
    public MatchMaker(Server server) {
        this.server = server;
        keepAlive = true;

        thread = new Thread(this);
        thread.setName("MatchMaker");
        thread.start();
    }

    /**
     * Main loop: sleeps briefly, then attempts to match pairs from the queue.
     * Creates and starts a new GameManager for each matched pair.
     */
    @Override
    public void run() {
        log.info("MatchMaker started");
        while (keepAlive) {
            try {
                Thread.sleep(500);

                Set<Connection> queue = server.getQueue();
                if (queue.size() < 2) {
                    continue;
                }

                Iterator<Connection> iterator = queue.iterator();

                while (iterator.hasNext() && keepAlive) {
                    Connection player1 = iterator.next();
                    if (!iterator.hasNext()) {
                        log.info("Odd number of players in queue");
                        break;
                    }
                    Connection player2 = iterator.next();

                    if (player1.getName() != null && player2.getName() != null &&
                            player1.getGameManager() == null && player2.getGameManager() == null) {

                        log.info("Matching players: {} and {}", player1.getName(), player2.getName());

                        server.removeConnection(player1);
                        server.removeConnection(player2);

                        GameManager gameManager = new GameManager(player1, player2, server);
                        server.addGame(gameManager);

                        Thread gameThread = new Thread(gameManager);
                        gameThread.setName("Game-" + player1.getName() + "-" + player2.getName());
                        gameThread.start();
                    }
                }

                server.cleanupGames();
            } catch (InterruptedException e) {
                log.error("MatchMaker interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("MatchMaker error", e);
            }
        }
        log.info("MatchMaker terminating");
    }

    /**
     * Signals the MatchMaker to stop matching and terminate its thread.
     */
    public void terminate() {
        log.info("Terminating MatchMaker");
        keepAlive = false;
    }
}
