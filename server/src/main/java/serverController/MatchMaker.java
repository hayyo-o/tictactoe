package serverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MatchMaker implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MatchMaker.class);

    private Server server;
    private volatile boolean keepAlive;
    private Thread thread;

    public MatchMaker(Server server) {
        this.server = server;
        keepAlive = true;

        thread = new Thread(this);
        thread.setName("MatchMaker");
        thread.start();
    }

    @Override
    public void run() {
        log.info("MatchMaker started");
        while(keepAlive) {
            try {
                Thread.sleep(500);
                Set<Connection> queue;
                synchronized (server.getQueue()) {
                    if(server.getQueue().size() < 2) {
                        continue;
                    }
                    queue = server.getQueue();
                }

                Iterator<Connection> iterator = queue.iterator();

                while(iterator.hasNext() && keepAlive) {
                    Connection player1 = iterator.next();
                    if (!iterator.hasNext()) {
                        log.info("Odd number of players in queue");
                        break;
                    }
                    Connection player2 = iterator.next();

                    if (player1.getName() != null && player2.getName() != null) {
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
        log.info("MatchMaker interrupted");
    }

    public void terminate() {
        log.info("Terminating MatchMaker");
        keepAlive = false;
    }
}
