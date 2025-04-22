package cz.tictactoe.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class Controller {
    @FXML private TextField usernameField;
    @FXML private StackPane viewStack;
    @FXML private VBox loginScreen;
    @FXML private VBox waitingScreen;
    @FXML private GridPane gameGrid;
    private char currentPlayer = 'X'; // X starts

    @FXML
    public void initialize() {
        initializeGameGrid();
    }

    private void initializeGameGrid() {
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

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Zadej jméno.").showAndWait();
            return;
        }
        showWaitingScreen();
        // TODO: Implement server connection logic here
    }

    private void showWaitingScreen() {
        loginScreen.setVisible(false);
        waitingScreen.setVisible(true);
        // TODO: Implement waiting logic and transition to game screen
    }

    private void showGameScreen() {
        waitingScreen.setVisible(false);
        gameGrid.setVisible(true);
    }

    private void makeMove(int x, int y, Button btn) {
        if (!btn.getText().isEmpty()) return; // Ignore move if the cell is occupied

        btn.setText(String.valueOf(currentPlayer));
        if (checkWin()) {
            // WIN massage
            System.out.println(currentPlayer + " wins!");
            gameGrid.setDisable(true);
            return;
        } else if (isBoardFull()) {
            // TIE massage
            System.out.println("Draw!");
            gameGrid.setDisable(true);
            return;
        }
        // TODO: Send move to server
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X'; // Changing player
    }

    private boolean checkWin() {
        // Extract the text of each button (X or O) into a matrix
        String[][] board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ((Button)gameGrid.getChildren().get(i * 3 + j)).getText();
            }
        }

        // Checking horizontals and verticals
        for (int i = 0; i < 3; i++) {
            if (!board[i][0].isEmpty() && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]))
                return true;
            if (!board[0][i].isEmpty() && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]))
                return true;
        }

        // Check the diagonals
        if (!board[0][0].isEmpty() && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]))
            return true;
        if (!board[0][2].isEmpty() && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]))
            return true;

        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String cell = ((Button)gameGrid.getChildren().get(i * 3 + j)).getText();
                if (cell.isEmpty()) {
                    return false; // At least 1 cell is empty
                }
            }
        }
        return true; // All cells are occupied
    }

}
