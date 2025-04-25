package serverController;

import clientEnumUtils.ClientEnumHandler;
import enums.ClientMessages;
import enums.ServerMessages;
import serverEnumUtils.ServerEnumHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serverEnumUtils.ServerMessageBuilder;

import java.io.*;
import java.net.Socket;


public class Connection implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    private Server server;
    private Socket socket;

    String username = null;

    private boolean ready = false;
    private GameManager gameManager;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private volatile boolean keepAlive = true;
    private final Object lock = new Object();

    public Connection(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            log.info("Creating IOStreams");
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            try {
                log.error("Failed to create IOStreams");
                socket.close();
            } catch (IOException ex) {
                log.error("Failed to close socket");
            }
        }
    }
    public void sendMessage(String message) {
        try {
            log.info("Sending message {}", message);
            oos.writeObject(message);
        } catch (IOException ex) {
            log.error("Failed to send message through oos");
        }
    }

    @Override
    public void run() {
        log.info("Connection to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        try {
            while(keepAlive) {
                String incomingMessage = ois.readObject().toString();
                log.info("Received message {}", incomingMessage);
                String[] command = incomingMessage.split(" ");
                ClientMessages keyword = ClientEnumHandler.enumFinder(command[0]);

                if(command[0].equals("")) {
                    command[0] = null;
                }

                if(keyword == ClientMessages.HELLO) {
                    // TODO check for invalid usernames and duplicates
                    username = command[1];
                    log.info("User {} logged in", username);
                    oos.writeObject(ServerMessageBuilder.welcome(username));
                    ready = true;
                } else if (keyword == ClientMessages.QUIT) {
                    log.info("User {} sent quit", username);
                    ready = false;
                    if(gameManager != null) {
                        gameManager.quit(this);
                    }
                    keepAlive = false;
                    oos.writeObject(ServerMessageBuilder.disconnect());
                } else if (keyword == null) {
                    log.error("Incorrect incoming message");
                    oos.writeObject(ServerMessageBuilder.error("InvalidCommand"));
                }
                else if (keyword == ClientMessages.OK) {
                    log.info("User {} sent ok", username);
                    gameManager.playerReady(this);
                } else {
                    // TODO player sent some nonsense after MOVE keyword
                    gameManager.playerMove(this, Integer.parseInt(command[1]), Integer.parseInt(command[2]));
                }

            }
        } catch (IOException e) {
            log.error("Failed to read incoming object");
        } catch (ClassNotFoundException e) {
            log.error("Incoming object error", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Failed to close socket");
            }
        }
    }

    public void close() {
        synchronized (lock) {
            keepAlive = false;
        }
    }

    public void terminate() {
        close();
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            log.error("Error closing connection resources", e);
        }
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public String getName() {
        return username;
    }
}
