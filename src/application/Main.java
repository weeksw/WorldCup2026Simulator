package application;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the 2026 FIFA World Cup Simulator Application.
 * <p>
 * Standard JavaFX execution life cycle that handles primary stage initialization, 
 * UI scene configuration, and delegates initial dashboard creation to {@link MainDashboard}.
 * </p>
 * 
 * @author William Weeks
 * @version 1.0
 */

public class Main extends Application {

    /**
     * Initializes and displays the primary window (Stage) for the application.
     * Sets up the main window title and renders the initial dashboard scene.
     * 
     * @param primaryStage The root stage provided by the JavaFX runtime.
     */
	
    @Override 
    public void start(Stage primaryStage) {
    	
        // Instantiate the main control dashboard with primary stage context
        MainDashboard dashboard = new MainDashboard(primaryStage);
        
        // Configure window parameters and transition to the entry scene
        primaryStage.setTitle("2026 FIFA World Cup Simulator");
        primaryStage.setScene(dashboard.createDashboardScene());
        primaryStage.show(); 
    }

    /**
     * Main application entry point. Invokes the JavaFX launch lifecycle.
     * 
     * @param args Command-line arguments passed upon execution.
     */
    
    public static void main(String[] args) {
    	
        launch(args);
    }
}