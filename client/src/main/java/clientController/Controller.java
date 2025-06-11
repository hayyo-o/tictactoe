package clientController;

import clientEnumUtils.ClientMessageBuilder;
import serverEnumUtils.ServerEnumHandler;
import enums.ServerMessages;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
     * Initializes the controller after FXML loading.
     * Sets up the empty game grid.
     */
    @FXML
    public void initialize() {
        log.info("Initializing game grid");
        initializeGameGrid();
    }

    /**
     * Creates the 3×3 grid of buttons used for the Tic Tac Toe board.
     * Each button, when clicked, will attempt to send a move to the server.
     */
    private void initializeGameGrid() {
        gameGrid.getChildren().clear();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button();
                button.setMinSize(100, 100);
                button.setStyle("-fx-background-color: white; -fx-border-color: #b0bec5; -fx-font-size: 24px; -fx-font-weight: bold;");
                int finalI = i, finalJ = j;
                button.setOnAction(event -> makeMove(finalI, finalJ, button));
                gameGrid.add(button, j, i);
            }
        }
    }

    /**
     * Handles the Login button click.
     * Validates the username, connects to the server, sends HELLO, and processes the response.
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
            if (response == null) {
                showAlert("No response from server.");
                log.error("Null response from server.");
                return;
            }

            String[] parts = response.split(" ");
            ServerMessages messageType = ServerEnumHandler.enumFinder(parts[0]);

            if (messageType == ServerMessages.WELCOME) {
                showWaitingScreen();
                new Thread(this::listenToServer).start();
            } else if (messageType == ServerMessages.ERROR) {
                StringBuilder errorMessage = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    errorMessage.append(parts[i]).append(" ");
                }
                String errorText = errorMessage.toString().trim();
                log.warn("Login failed: {}", errorText);
                showAlert("Error: " + errorText);
                socket.close();
            } else {
                log.error("Unexpected login response: {}", response);
                showAlert("Unexpected response from server: " + response);
                socket.close();
            }

        } catch (IOException e) {
            log.error("Failed to connect to server.", e);
            showAlert("Failed to connect to server.");
        }
    }

    /**
     * Handles the Quit button click.
     * Sends QUIT, closes the socket, and resets the UI to the login screen.
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
     * Continuously listens for messages from the server and dispatches handling
     * onto the JavaFX application thread when UI updates are needed.
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
                        String player1 = parts[1];
                        String player2 = parts[2];
                        opponent = player1.equals(username) ? player2 : player1;

                        mySymbol = player1.equals(username) ? "X" : "O";
                        opponentSymbol = mySymbol.equals("X") ? "O" : "X";

                        Platform.runLater(() -> {
                            showGameScreen();
                            playerLabel.setText("You: " + username + " (" + mySymbol + ")");
                            opponentLabel.setText("Opponent: " + opponent + " (" + opponentSymbol + ")");
                            setStatusLabel("Waiting...");
                        });
                    }
                    case YOUR_TURN -> {
                        isMyTurn = parts[1].equals(username);
                        Platform.runLater(() -> {
                            gameGrid.setDisable(!isMyTurn);
                            setStatusLabel(isMyTurn ? "Status: Your turn" : "Status: Opponent's turn");
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
                            gameGrid.setDisable(true);
                            setStatusLabel(winner.equals(username) ? "You won!" : "You lost.");
                            showAlert(winner.equals(username) ? "You won!" : "You lost.");
                        });
                    }
                    case DRAW -> Platform.runLater(() -> {
                        gameGrid.setDisable(true);
                        setStatusLabel("Draw");
                        showAlert("The game ended in a draw!");
                    });
                    case ERROR -> {
                        StringBuilder errorMessage = new StringBuilder();
                        for (int i = 1; i < parts.length; i++) {
                            errorMessage.append(parts[i]).append(" ");
                        }
                        Platform.runLater(() -> showAlert("Error: " + errorMessage.toString().trim()));
                    }
                    case DISCONNECT -> Platform.runLater(() -> {
                        showAlert("Opponent disconnected.");
                        setStatusLabel("Opponent left the game.");
                        gameGrid.setDisable(true);
                    });
                    default -> log.warn("Unknown command received from server: {}", parts[0]);
                }
            }
        } catch (IOException e) {
            if (socket.isClosed()) {
                log.info("Socket closed, listener thread exiting normally.");
            } else {
                log.error("Connection to server lost.", e);
                Platform.runLater(() -> showAlert("Connection to server lost."));
            }
        }
    }

    /**
     * Marks the given move on the local game grid UI.
     *
     * @param player the player who made the move
     * @param x      row index (0–2)
     * @param y      column index (0–2)
     */
    private void markMove(String player, int x, int y) {
        Button btn = getButtonAt(x, y);
        if (btn != null && btn.getText().isEmpty()) {
            btn.setText(player.equals(username) ? mySymbol : opponentSymbol);
            btn.setStyle("-fx-text-fill: " + (player.equals(username) ? "#1976d2" : "#616161") + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        }
    }

    /**
     * Sends a move command to the server if it's the client's turn and the cell is empty.
     *
     * @param x   row index (0–2)
     * @param y   column index (0–2)
     * @param btn the UI button representing the cell
     */
    private void makeMove(int x, int y, Button btn) {
        if (!btn.getText().isEmpty() || !isMyTurn) return;

        out.println(ClientMessageBuilder.move(x, y));
        log.info("Move sent: ({}, {})", x, y);
        isMyTurn = false;
        Platform.runLater(() -> {
            setStatusLabel("Opponent's turn");
            gameGrid.setDisable(true);
        });
    }

    /**
     * Finds the button in the grid at the specified coordinates.
     *
     * @param row row index (0–2)
     * @param col column index (0–2)
     * @return the Button at that position, or {@code null} if none found
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
     * Switches the UI to the waiting‐for‐match screen.
     */
    private void showWaitingScreen() {
        loginScreen.setVisible(false);
        waitingScreen.setVisible(true);
        gameScreen.setVisible(false);
    }

    /**
     * Clears all marks from the game grid and resets button styles.
     */
    private void resetGameGrid() {
        for (javafx.scene.Node node : gameGrid.getChildren()) {
            if (node instanceof Button) {
                ((Button) node).setText("");
                node.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-font-size: 24px; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Switches the UI to the active game screen.
     */
    private void showGameScreen() {
        loginScreen.setVisible(false);
        waitingScreen.setVisible(false);
        gameScreen.setVisible(true);
    }

    /**
     * Updates the status label with the given message.
     *
     * @param message the new status text
     */
    private void setStatusLabel(String message) {
        statusLabel.setVisible(true);
        statusLabel.setText(message);
    }

    /**
     * Displays an informational alert dialog with the given message.
     *
     * @param message the content text of the alert
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
