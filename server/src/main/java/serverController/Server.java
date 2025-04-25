package serverController;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.*;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final Object queueLock = new Object();
    private final Set<Connection> queue;
    private final Set<GameManager> gameManagerSet;

    private final Acceptor acceptor;
    private final MatchMaker mm;

    public static void main(String[] args) {
        log.info("Server started");

        Server server = new Server();

        System.out.println("Server started, press Enter to stop");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        log.info("Server is terminated");
        server.terminate();
    }

    public Server() {
        queue = new HashSet<Connection>();
        gameManagerSet = new HashSet<GameManager>();

        acceptor = new Acceptor(this, 8080);
        mm = new MatchMaker(this);
    }

    public synchronized void addGame(GameManager gameManager) {
        gameManagerSet.add(gameManager);
    }
    public synchronized void removeGame(GameManager gameManager) {
        gameManagerSet.remove(gameManager);
    }
    public synchronized void cleanupGames() {
        Iterator<GameManager> iterator = gameManagerSet.iterator();
        while (iterator.hasNext()) {
            GameManager gameManager = iterator.next();
            if(!gameManager.isGameRunning()) {
                iterator.remove();
            }
        }
    }

    public void terminate() {
        acceptor.terminate();
        mm.terminate();
        synchronized(queue) {
            for(Connection connection : new HashSet<>(queue)) {
                connection.terminate();
            }
        }
        synchronized(gameManagerSet) {
            for(GameManager gameManager : new HashSet<>(gameManagerSet)) {
                gameManager.terminate();
            }
        }
    }

    public void addConnection(Socket clientSocket) {
        log.info("Creating new connection with client");
        Connection connection = new Connection(this, clientSocket);
        Thread connectionThread = new Thread(connection);
        connectionThread.start();
        synchronized (queue) {
            queue.add(connection);
        }
    }

    public void removeConnection(Connection connection) {
        synchronized (queue) {
            queue.remove(connection);
        }
    }
    public void addExistingConnection(Connection connection) {
        log.info("Adding existing player {} to queue", connection.getName());
        synchronized (queue) {
            queue.add(connection);
        }
    }
    public Set<Connection> getQueue() {
        synchronized(queueLock) {
            return new HashSet<>(queue);
        }
    }




}