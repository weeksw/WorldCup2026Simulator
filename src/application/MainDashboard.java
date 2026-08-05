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
        
        // Populate Dropdown with teams from the manager
        // (Assuming tournamentManager initialized 48 teams)
        for (Team t : tournamentManager.getKnockoutTeamsStage32()) { 
             // Note: After group setup, you can loop all 48 initial teams
        }
        teamSelectionBox.getItems().addAll("Argentina", "France", "Spain", "England", "Brazil", "USA", "Mexico", "Canada");

        // Dynamic Toggle Logic
        modeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (modeGroup.getSelectedToggle() == followTeamBtn) {
                teamSelectionBox.setDisable(false);
            } else {
                teamSelectionBox.setDisable(true);
            }
        });

        // Tournament Layout Selection Checkbox
        CheckBox shuffleGroupsCheck = new CheckBox("Shuffle Groups Randomly");

        centerControls.getChildren().addAll(
            modeLabel, birdsEyeBtn, followTeamBtn, teamSelectionBox, new Separator(), shuffleGroupsCheck
        );
        root.setCenter(centerControls);

        // Bottom Action Button
        Button startButton = new Button("Launch Tournament Engine");
        startButton.setStyle("-fx-background-color: #2b6cb0; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        
        startButton.setOnAction(e -> {
            boolean shuffle = shuffleGroupsCheck.isSelected();
            
            // 1. Initialize Groups
            tournamentManager.setupGroups(shuffle);
            
            // 2. Capture User Mode Selection
            String selectedMode = birdsEyeBtn.isSelected() 
                ? "Birds-Eye Mode" 
                : "Tracking: " + (teamSelectionBox.getValue() != null ? teamSelectionBox.getValue() : "Default");
            
            // 3. Construct and Switch to the Group Stage Scene
            GroupStageView groupStageView = new GroupStageView(primaryStage, tournamentManager, selectedMode);
            primaryStage.setScene(groupStageView.createGroupStageScene());
        });

        VBox bottomBox = new VBox(startButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        root.setBottom(bottomBox);

        return new Scene(root, 700, 500);
    }
}