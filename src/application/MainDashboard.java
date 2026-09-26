package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Primary user interface dashboard for the 2026 FIFA World Cup Simulator.
 * <p>
 * Manages simulation mode selection (Spectate vs. Team Tracking), configuration
 * options (group shuffling), input validation, and initial view navigation.
 * </p>
 * 
 * @author William Weeks
 * @version 1.0
 */

public class MainDashboard {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private ComboBox<String> teamSelectionBox;
    private RadioButton birdsEyeBtn;
    private RadioButton followTeamBtn;
    private CheckBox shuffleGroupsCheck;

    /**
     * Constructs the MainDashboard and initializes the internal state engine.
     * 
     * @param primaryStage The primary JavaFX window frame for scene switching.
     */
    
    public MainDashboard(Stage primaryStage) {
    	
        this.primaryStage = primaryStage;
        this.tournamentManager = new TournamentManager();
    }

    /**
     * Builds and constructs the core layout scene for the main entry dashboard.
     * <p>
     * Assembles UI layout controls, registers reactive state listeners for simulation modes,
     * and attaches event validation logic prior to engine execution.
     * </p>
     * 
     * @return Scene The configured JavaFX Scene object ready for rendering.
     */
    
    @SuppressWarnings(value = {"unused"})
    public Scene createDashboardScene() {
    	
        // Root Layout Container
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));

        // Top Header View
        Label headerLabel = new Label("2026 FIFA World Cup Simulator");
        headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        VBox topBox = new VBox(headerLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(topBox);

        // Configuration Control Panel
        VBox centerControls = new VBox(15);
        centerControls.setAlignment(Pos.CENTER);
        centerControls.setMaxWidth(400);
        centerControls.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        // Simulation Mode Controls
        Label modeLabel = new Label("Select Simulation Mode:");
        modeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ToggleGroup modeGroup = new ToggleGroup();
        birdsEyeBtn = new RadioButton("Birds-Eye View (Simulate whole tournament)");
        birdsEyeBtn.setToggleGroup(modeGroup);
        birdsEyeBtn.setSelected(true);

        followTeamBtn = new RadioButton("Follow a Specific Country");
        followTeamBtn.setToggleGroup(modeGroup);

        // Specific Team Dropdown Selection
        teamSelectionBox = new ComboBox<>();
        teamSelectionBox.setPromptText("Choose your team...");
        teamSelectionBox.setDisable(true); // Disabled by default in Spectate mode
        
        teamSelectionBox.getItems().addAll(
            "Argentina", "France", "Spain", "England", "Brazil", "USA", "Mexico", "Canada",
            "Netherlands", "Portugal", "Germany", "Colombia", "Croatia", "Morocco", "Uruguay"
        );

        // Reactive UI State Listener (Enables drop down only when team tracking is active)
        modeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (modeGroup.getSelectedToggle() == followTeamBtn) {
                teamSelectionBox.setDisable(false);
            } else {
                teamSelectionBox.setDisable(true);
            }
        });

        // Group Seeding Checkbox Control
        shuffleGroupsCheck = new CheckBox("Shuffle Groups Randomly");

        centerControls.getChildren().addAll(
            modeLabel, birdsEyeBtn, followTeamBtn, teamSelectionBox, new Separator(), shuffleGroupsCheck
        );
        root.setCenter(centerControls);

        // Execution Action Trigger
        Button startButton = new Button("Launch Tournament Engine");
        startButton.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        
        // Event Handler: Validates inputs and initializes tournament engine state
        startButton.setOnAction(e -> {
        	
            // Validation path for "Follow a Specific Country" Mode
            if (followTeamBtn.isSelected()) {
            	
                String selectedTeam = teamSelectionBox.getValue();

                boolean isInvalidTeam = selectedTeam == null 
                        || selectedTeam.trim().isEmpty() 
                        || selectedTeam.equalsIgnoreCase("Choose your team...")
                        || selectedTeam.equalsIgnoreCase("Select a Team...")
                        || selectedTeam.equalsIgnoreCase("Default");

                // Block transition if team selection is omitted
                if (isInvalidTeam) {
                	
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Selection Required");
                    alert.setHeaderText("No Team Selected");
                    alert.setContentText("Please select a specific team to follow, or switch back to 'Birds-Eye View'.");
                    alert.showAndWait();
                    
                    return; // Prevent execution and hold user on current view
                }
                
                tournamentManager.setMode("Tracking");
                tournamentManager.setUserChosenTeamName(selectedTeam);

            } else {
            	
                // Configures Spectate mode and clears team dependencies
                tournamentManager.setMode("Spectate");
                tournamentManager.setUserChosenTeamName("");
            }

            // Seed/initialize group configurations
            tournamentManager.setupGroups(shuffleGroupsCheck.isSelected());

            // Transition scene frame to Group Stage
            showGroupStageScene();
        });

        VBox bottomBox = new VBox(startButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        root.setBottom(bottomBox);

        return new Scene(root, 700, 500);
    }

    /**
     * Swaps the active stage scene over to the {@link GroupStageView}.
     */
    private void showGroupStageScene() {
    	
        GroupStageView groupStageView = new GroupStageView(primaryStage, tournamentManager, tournamentManager.getMode());
        Scene groupStageScene = groupStageView.createGroupStageScene();
        primaryStage.setScene(groupStageScene);
    }
}