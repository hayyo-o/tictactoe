package clientController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for launching the Tic Tac Toe JavaFX client.
 *
 * <p>Loads the FXML layout, sets up the primary stage, and shows the GUI.
 *
 * @author Pavel Liapin
 * @version 1.0
 * @created April 2025
 */
public class App extends Application {

  /**
   * Entry point for JavaFX; called after {@link #main(String[])}.
   *
   * <p>Initializes and displays the primary stage with the game layout.
   *
   * @param stage the primary stage provided by the JavaFX runtime
   * @throws Exception if the FXML resource cannot be loaded
   */
  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout.fxml"));
    Scene scene = new Scene(loader.load());
    stage.setTitle("Tic Tac Toe");
    stage.setScene(scene);
    stage.show();
  }

  /**
   * Standard Java entry point; launches the JavaFX application.
   *
   * @param args command‐line arguments (unused)
   */
  public static void main(String[] args) {
    launch(args);
  }
}
