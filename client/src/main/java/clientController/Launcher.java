package clientController;

/**
 * Launcher class for JavaFX application to avoid module issues.
 * This class is NOT a subclass of javafx.application.Application.
 * It simply calls the main method of the actual JavaFX Application class.
 *
 * @author Pavel Liapin
 * @version 1.0
 * @created June 2025
 */
public class Launcher {

    /**
     * Main method that launches the JavaFX application.
     * This workaround helps create a fat JAR without dealing with JavaFX modules.
     *
     * @param args command line arguments passed to the JavaFX application
     */
    public static void main(String[] args) {
        // Simply call the main method of the JavaFX Application class
        App.main(args);
    }
}