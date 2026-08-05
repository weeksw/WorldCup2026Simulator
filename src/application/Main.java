/**
 * 
 */
package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainDashboard dashboard = new MainDashboard(primaryStage);
        
        primaryStage.setTitle("2026 FIFA World Cup Simulator");
        primaryStage.setScene(dashboard.createDashboardScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}