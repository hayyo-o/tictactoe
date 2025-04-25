package serverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Acceptor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);

    private Server server;
    private Thread thread;
    private volatile boolean keepAlive;


    private ServerSocket serverSocket;
    private int port;

    public Acceptor(Server server, int port) {
        this.server = server;
        this.port = port;
        keepAlive = true;

        thread = new Thread(this);
        thread.setName("Acceptor");
        thread.start();
    }


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

    private void close() {
        if(serverSocket == null) {
            log.debug("Server socket is null, terminating Acceptor");
        } else {
            log.debug("Closing server socket and terminating Acceptor");
            try {
                serverSocket.close();
            } catch(IOException e) {
                log.debug("Error closing server socket", e);
            }
        }
    }

    public void terminate() {
        log.info("Terminating Acceptor");
        keepAlive = false;
        close();
    }
}
