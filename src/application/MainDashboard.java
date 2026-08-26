package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainDashboard {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private ComboBox<String> teamSelectionBox;
    private RadioButton birdsEyeBtn;
    private RadioButton followTeamBtn;
    private CheckBox shuffleGroupsCheck;

    public MainDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.tournamentManager = new TournamentManager();
    }

    public Scene createDashboardScene() {
        // Master Container
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));

        // Top Header
        Label headerLabel = new Label("2026 FIFA World Cup Simulator");
        headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        VBox topBox = new VBox(headerLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(topBox);

        // Center Option Controls
        VBox centerControls = new VBox(15);
        centerControls.setAlignment(Pos.CENTER);
        centerControls.setMaxWidth(400);
        centerControls.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        // Game Mode Toggles
        Label modeLabel = new Label("Select Simulation Mode:");
        modeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ToggleGroup modeGroup = new ToggleGroup();
        birdsEyeBtn = new RadioButton("Birds-Eye View (Simulate whole tournament)");
        birdsEyeBtn.setToggleGroup(modeGroup);
        birdsEyeBtn.setSelected(true);

        followTeamBtn = new RadioButton("Follow a Specific Country");
        followTeamBtn.setToggleGroup(modeGroup);

        // Team Dropdown (Disabled by default unless 'Follow a Specific Country' is picked)
        teamSelectionBox = new ComboBox<>();
        teamSelectionBox.setPromptText("Choose your team...");
        teamSelectionBox.setDisable(true);
        
        teamSelectionBox.getItems().addAll(
            "Argentina", "France", "Spain", "England", "Brazil", "USA", "Mexico", "Canada",
            "Netherlands", "Portugal", "Germany", "Colombia", "Croatia", "Morocco", "Uruguay"
        );

        // Dynamic Toggle Logic
        modeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (modeGroup.getSelectedToggle() == followTeamBtn) {
                teamSelectionBox.setDisable(false);
            } else {
                teamSelectionBox.setDisable(true);
            }
        });

        // Tournament Layout Selection Checkbox
        shuffleGroupsCheck = new CheckBox("Shuffle Groups Randomly");

        centerControls.getChildren().addAll(
            modeLabel, birdsEyeBtn, followTeamBtn, teamSelectionBox, new Separator(), shuffleGroupsCheck
        );
        root.setCenter(centerControls);

        // Bottom Action Button
        Button startButton = new Button("Launch Tournament Engine");
        startButton.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        
        startButton.setOnAction(e -> {
            // 1. Validation for "Follow a Specific Country" Mode
            if (followTeamBtn.isSelected()) {
                String selectedTeam = teamSelectionBox.getValue();

                boolean isInvalidTeam = selectedTeam == null 
                        || selectedTeam.trim().isEmpty() 
                        || selectedTeam.equalsIgnoreCase("Choose your team...")
                        || selectedTeam.equalsIgnoreCase("Select a Team...")
                        || selectedTeam.equalsIgnoreCase("Default");

                if (isInvalidTeam) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Selection Required");
                    alert.setHeaderText("No Team Selected");
                    alert.setContentText("Please select a specific team to follow, or switch back to 'Birds-Eye View'.");
                    alert.showAndWait();
                    
                    return; // Blocks transition to the group stage
                }
                
                tournamentManager.setMode("Tracking");
                tournamentManager.setUserChosenTeamName(selectedTeam);

            } else {
                // 2. Birds-Eye View / Spectate mode
                tournamentManager.setMode("Spectate");
                tournamentManager.setUserChosenTeamName("");
            }

            // Initialize groups based on checkbox selection
            tournamentManager.setupGroups(shuffleGroupsCheck.isSelected());

            // Transition to the Group Stage View
            showGroupStageScene();
        });

        VBox bottomBox = new VBox(startButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        root.setBottom(bottomBox);

        return new Scene(root, 700, 500);
    }

    /**
     * Swaps the stage scene over to the GroupStageView.
     */
    private void showGroupStageScene() {
        // Pass tournamentManager.getMode() OR tournamentManager.getUserChosenTeamName() as the 3rd argument
        GroupStageView groupStageView = new GroupStageView(primaryStage, tournamentManager, tournamentManager.getMode());
        Scene groupStageScene = groupStageView.createGroupStageScene();
        primaryStage.setScene(groupStageScene);
    }
}