package serverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens for incoming client connections on a specified port
 * and hands them off to the {@link Server}.
 *
 * <p>Runs in its own thread, continuously accepting new sockets until terminated.</p>
 *
 * @version 1.0
 * @created April 2025
 */
public class Acceptor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);

    private Server server;
    private Thread thread;
    private volatile boolean keepAlive;
    private ServerSocket serverSocket;
    private int port;

    /**
     * Constructs and starts an Acceptor that listens on the given port.
     *
     * @param server the server instance to which new connections are added
     * @param port   the TCP port to listen on
     */
    public Acceptor(Server server, int port) {
        this.server = server;
        this.port = port;
        keepAlive = true;

        thread = new Thread(this);
        thread.setName("Acceptor");
        thread.start();
    }

    /**
     * Main loop: opens the ServerSocket and accepts new client connections.
     * Each accepted socket is passed to {@link Server#addConnection(Socket)}.
     */
    @Override
    public void run() {
        log.info("Acceptor thread started");
        try {
            serverSocket = new ServerSocket(port);
            while (keepAlive) {
                log.info("Waiting for client on port {}", port);
                Socket socket = serverSocket.accept();
                log.debug("New client connected");
                server.addConnection(socket);
            }
        } catch (IOException e) {
            if(keepAlive) {
                log.error("Exception appeared while waiting for connection", e);
            }
        } finally {
            close();
        }
        log.info("Acceptor thread terminated");
    }

    /**
     * Shuts down the acceptor: stops accepting and closes the server socket.
     */
    public void terminate() {
        log.info("Terminating Acceptor");
        keepAlive = false;
        close();
    }

    /**
     * Closes the ServerSocket if open.
     */
    private void close() {
        if (serverSocket == null) {
            log.debug("Server socket is null, nothing to close");
        } else {
            log.debug("Closing server socket");
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.debug("Error closing server socket", e);
            }
        }
    }
}
