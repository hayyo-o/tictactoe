package clientController;

import clientEnumUtils.ClientMessageBuilder;
import serverEnumUtils.ServerEnumHandler;
import enums.ServerMessages;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Controller class manages the user interface logic for the Tic Tac Toe client application.
 * It handles user login, game moves, communication with the server, and updating the game screen.
 *
 * This class is part of a networked desktop version of the Tic Tac Toe game.
 *
 * @author Pavel Liapin
 * @version 1.0
 * @created April 2025
 */
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @FXML private TextField usernameField;
    @FXML private VBox loginScreen;
    @FXML private VBox waitingScreen;
    @FXML private VBox gameScreen;
    @FXML private GridPane gameGrid;
    @FXML private Label statusLabel;
    @FXML private Label playerLabel;
    @FXML private Label opponentLabel;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String opponent;
    private String mySymbol;
    private String opponentSymbol;
    private boolean isMyTurn = false;

    /**
     * Initializes the controller by setting up the game grid.
     */
    @FXML
    public void initialize() {
        log.info("Initializing game grid");
        initializeGameGrid();
    }

    /**
     * Sets up the buttons for the 3x3 game grid.
     */
    private void initializeGameGrid() {
        gameGrid.getChildren().clear();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button();
                button.setMinSize(100, 100);
                int finalI = i, finalJ = j;
                button.setOnAction(event -> makeMove(finalI, finalJ, button));
                gameGrid.add(button, j, i);
            }
        }
    }

    /**
     * Handles user login and server connection.
     */
    @FXML
    private void handleLogin() {
        username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showAlert("Please enter your username.");
            return;
        }

        try {
            log.info("Connecting to server...");
            socket = new Socket("localhost", 8080);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(ClientMessageBuilder.hello(username));
            log.info("Sent username to server: {}", username);

            String response = in.readLine();
            String[] welcomeParts = response.split(" ");
            if (ServerEnumHandler.enumFinder(welcomeParts[0]) == ServerMessages.WELCOME) {
                showWaitingScreen();
                new Thread(this::listenToServer).start();
            } else {
                showAlert("Server refused connection.");
                log.error("Server refused connection.");
            }

        } catch (Exception e) {
            log.error("Failed to connect to server.", e);
            showAlert("Failed to connect to server.");
        }
    }

    /**
     * Handles the user quitting the game and returns to the login screen.
     */
    @FXML
    private void handleQuit() {
        try {
            if (out != null) {
                out.println(ClientMessageBuilder.quit());
                log.info("Sent quit request to server.");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log.info("Socket closed.");
            }
        } catch (IOException e) {
            log.error("Error while closing socket.", e);
        }

        Platform.runLater(() -> {
            resetGameGrid();
            loginScreen.setVisible(true);
            waitingScreen.setVisible(false);
            gameScreen.setVisible(false);
            statusLabel.setText("");
        });
    }

    /**
     * Listens for server messages and processes them accordingly.
     */
    private void listenToServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                log.info("Received message from server: {}", line);
                String[] parts = line.split(" ");
                ServerMessages command = ServerEnumHandler.enumFinder(parts[0]);

                switch (command) {
                    case START -> {
                        Platform.runLater(this::showGameScreen);
                        String player1 = parts[1];
                        String player2 = parts[2];

                        opponent = player1.equals(username) ? player2 : player1;

                        Platform.runLater(() -> {
                            playerLabel.setText("You: " + username + " " + mySymbol);
                            opponentLabel.setText("Opponent: " + opponent + " " + opponentSymbol);
                            setStatusLabel(isMyTurn ? "Status: your turn" : "Status: opponent's turn");
                        });

                        if (username.equals(player1)) {
                            mySymbol = "X";
                            opponentSymbol = "O";
                        } else {
                            mySymbol = "O";
                            opponentSymbol = "X";
                        }
                    }

                    case YOUR_TURN -> {
                        isMyTurn = parts[1].equals(username);
                        Platform.runLater(() -> {
                            setStatusLabel(isMyTurn ? "Status: your turn" : "Status: opponent's turn");
                            gameGrid.setDisable(!isMyTurn);
                        });
                    }

                    case MOVE -> {
                        String player = parts[1];
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        Platform.runLater(() -> markMove(player, x, y));
                    }

                    case WINNER -> {
                        String winner = parts[1];
                        Platform.runLater(() -> {
                            showAlert(winner.equals(username) ? "You won!" : "You lost.");
                            setStatusLabel("Status: " + winner + " won!");
                            gameGrid.setDisable(true);
                        });
                    }

                    case DRAW -> Platform.runLater(() -> {
                        showAlert("The game ended in a draw!");
                        setStatusLabel("Status: draw");
                        gameGrid.setDisable(true);
                    });

                    case ERROR -> {
                        StringBuilder errorMessage = new StringBuilder();
                        for (int i = 1; i < parts.length; i++) {
                            errorMessage.append(parts[i]).append(" ");
                        }
                        Platform.runLater(() -> showAlert("Error: " + errorMessage.toString().trim()));
                    }

                    case DISCONNECT -> Platform.runLater(() -> showAlert("Opponent disconnected."));

                    default -> log.warn("Unknown command received from server: {}", parts[0]);
                }
            }
        } catch (IOException e) {
            log.error("Connection to server lost.", e);
            Platform.runLater(() -> showAlert("Connection to server lost."));
        }
    }

    /**
     * Marks the move made by a player on the game grid.
     *
     * @param player Player who made the move
     * @param x Row index
     * @param y Column index
     */
    private void markMove(String player, int x, int y) {
        Button btn = getButtonAt(x, y);
        if (btn != null && btn.getText().isEmpty()) {
            btn.setText(player.equals(username) ? mySymbol : opponentSymbol);
        }
    }

    /**
     * Sends the player's move to the server.
     *
     * @param x Row index
     * @param y Column index
     * @param btn Button clicked
     */
    private void makeMove(int x, int y, Button btn) {
        if (!btn.getText().isEmpty() || !isMyTurn) return;

        out.println(ClientMessageBuilder.move(x, y));
        log.info("Move sent: ({}, {})", x, y);
        isMyTurn = false;
        Platform.runLater(() -> {
            setStatusLabel(isMyTurn ? "Status: your turn" : "Status: opponent's turn");
            gameGrid.setDisable(true);
        });
    }

    /**
     * Gets the button at the specified row and column.
     *
     * @param row Row index
     * @param col Column index
     * @return Button at the specified position or null
     */
    private Button getButtonAt(int row, int col) {
        for (javafx.scene.Node node : gameGrid.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Button) node;
            }
        }
        return null;
    }

    /**
     * Displays the waiting screen while searching for an opponent.
     */
    private void showWaitingScreen() {
        loginScreen.setVisible(false);
        waitingScreen.setVisible(true);
        gameScreen.setVisible(false);
    }

    /**
     * Resets the game grid by clearing all the buttons.
     */
    private void resetGameGrid() {
        for (javafx.scene.Node node : gameGrid.getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setText("");
            }
        }
    }

    /**
     * Displays the game screen when the game starts.
     */
    private void showGameScreen() {
        loginScreen.setVisible(false);
        waitingScreen.setVisible(false);
        gameScreen.setVisible(true);
    }

    /**
     * Updates the status label with the current player's turn information.
     *
     * @param message Message to display
     */
    private void setStatusLabel(String message) {
        statusLabel.setVisible(true);
        statusLabel.setText(message);
    }

    /**
     * Shows an alert dialog with a given message.
     *
     * @param message Message to display
     */
    private void showAlert(String message) {
        log.info("Showing alert: {}", message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText("Message");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
