package serverController;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.*;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final Deque<Connection> queue;
    private final Set<GameManager> gameManagerSet;

    private final Acceptor acceptor;

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
        queue = new LinkedList<Connection>();
        gameManagerSet = new HashSet<GameManager>();

        acceptor = new Acceptor(this, 8080);
    }

    public void terminate() {
        acceptor.terminate();
        // TODO CLOSE ALL CONNECTIONS
    }

    public void addConnection(Socket clientSocket) {
        log.info("Creating new connection with client");
        Connection connection = new Connection(this, clientSocket);
        Thread connectionThread = new Thread(connection);
        connectionThread.start();
        synchronized (queue) {
            queue.addLast(connection);
        }
    }

    public void removeConnection(Connection connection) {
        synchronized (queue) {
            queue.remove(connection);
        }
    }




}