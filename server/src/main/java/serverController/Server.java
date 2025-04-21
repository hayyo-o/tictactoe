package serverController;

import ch.qos.logback.core.joran.sanity.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final Deque<Connection> queue;
    private final Set<Pair> pairs;


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
        pairs = new HashSet<Pair>();
    }
}