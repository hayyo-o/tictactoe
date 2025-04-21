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

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Connection(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public void sendMessage(String message) {
        try {
            oos.writeObject(message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        log.info("Connection to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        try {
            boolean keepAlive = true;
            String username = null;

            while(keepAlive) {
                String incomingMessage = (String) ois.readObject();
                String[] command = incomingMessage.split(" ");
                ClientMessages keyword = ClientEnumHandler.enumFinder(command[0]);

                // Empty string as keyword
                if(command[0].equals("")) {
                    command[0] = null;
                }

                if(keyword == ClientMessages.HELLO) {
                    username = command[1];
                    log.info("User {} logged in", username);
                    oos.writeObject(ServerMessageBuilder.welcome(username));
                    // TODO add connection to queue
                } else if (keyword == ClientMessages.QUIT) {
                    log.info("User {} sent quit", username);
                    keepAlive = false;
                    oos.writeObject(ServerMessageBuilder.disconnect());
                } else if (keyword == null) {
                    log.error("Incorrect incoming message");
                    oos.writeObject(ServerMessageBuilder.error("InvalidCommand"));
                }
                else {
                    // TODO game logic new class....
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
