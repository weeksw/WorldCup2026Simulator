/**
 * 
 */
package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a simple UI layout
        Label label = new Label("Welcome to the 2026 FIFA World Cup Simulator!");
        StackPane root = new StackPane(label);
        
        // Define the window dimensions
        Scene scene = new Scene(root, 500, 300);
        
        // Setup and show the window frame
        primaryStage.setTitle("2026 World Cup Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Launches the JavaFX application life cycle
        launch(args);
    }
}