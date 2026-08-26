package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GroupStageView {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private String selectedMode;
    private String trackedTeamName;
    private ScrollPane centerContainer;
    private Button simButton;
    private Button nextStageButton;

    public GroupStageView(Stage primaryStage, TournamentManager tournamentManager, String selectedMode) {
        this.primaryStage = primaryStage;
        this.tournamentManager = tournamentManager;
        this.selectedMode = selectedMode;

        // Extract team name if mode is "Tracking: TeamName"
        if (selectedMode.startsWith("Tracking: ")) {
            this.trackedTeamName = selectedMode.replace("Tracking: ", "").trim();
            // Sync user's focus team with TournamentManager
            this.tournamentManager.setUserChosenTeamName(this.trackedTeamName);
        }
    }

    private void checkUserTeamElimination() {
        String userTeam = tournamentManager.getUserChosenTeamName();
        
        // Safety check: Skip if spectating or if team is invalid/empty/default
        if ("Spectate".equalsIgnoreCase(tournamentManager.getMode()) 
                || userTeam == null 
                || userTeam.trim().isEmpty() 
                || userTeam.equalsIgnoreCase("Default")
                || userTeam.equalsIgnoreCase("Select a Team...")) {
            return;
        }

        // Safely check if the team qualified
        boolean qualified = tournamentManager.didTeamQualifyForKnockout(userTeam);

        if (!qualified) {
            promptUserTeamEliminated(userTeam);
        }
    }
    
    public Scene createGroupStageScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top Header
        Label titleLabel = new Label("Group Stage - 2026 FIFA World Cup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        simButton = new Button("Simulate All Group Matches");
        simButton.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        nextStageButton = new Button("Advance to Knockout Stage ->");
        nextStageButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nextStageButton.setDisable(true);

        nextStageButton.setOnAction(e -> {
            // Pass 'primaryStage.getScene()' so KnockoutStageView can navigate back seamlessly
            KnockoutStageView knockoutView = new KnockoutStageView(primaryStage, tournamentManager, selectedMode, primaryStage.getScene());
            primaryStage.setScene(knockoutView.createKnockoutScene());
        });
        
        HBox topBar = new HBox(15, titleLabel, simButton, nextStageButton);

        // If tracking a team, add a button to open the external "All Groups" window
        if (trackedTeamName != null) {
            Button viewAllBtn = new Button("Explore All Groups 🌐");
            viewAllBtn.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            viewAllBtn.setOnAction(e -> openAllGroupsWindow());
            topBar.getChildren().add(viewAllBtn);
        }

        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBar);

        // Center Container
        centerContainer = new ScrollPane();
        centerContainer.setFitToWidth(true);
        centerContainer.setContent(buildCenterContent());
        root.setCenter(centerContainer);

        // Check if group stage has already been simulated
        if (isGroupStageAlreadySimulated()) {
            simButton.setDisable(true);
            simButton.setText("Completed ✓");
            nextStageButton.setDisable(false);
        }

     // Simulation Button Action
        simButton.setOnAction(e -> {
            tournamentManager.runGroupStage();
            centerContainer.setContent(buildCenterContent()); // Refresh table data visually
            simButton.setDisable(true);
            simButton.setText("Completed ✓");
            nextStageButton.setDisable(false);

            // Call the updated elimination checking logic
            checkUserTeamElimination();
        });

        return new Scene(root, 1100, 750);
    }

    private boolean isGroupStageAlreadySimulated() {
        for (Group g : tournamentManager.getGroups()) {
            for (Match m : g.getMatches()) {
                if (m.isPlayed()) return true;
            }
        }
        return false;
    }

    private void promptUserTeamEliminated(String teamName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Team Eliminated");
        alert.setHeaderText("❌ " + teamName + " Has Been Knocked Out!");
        alert.setContentText(teamName + " failed to qualify for the Knockout Stage. Would you like to view the remainder of the tournament as a spectator or start over?");

        ButtonType viewRemainderBtn = new ButtonType("Spectate Remainder");
        ButtonType startOverBtn = new ButtonType("Start Over", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(viewRemainderBtn, startOverBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == startOverBtn) {
                try {
                    new Main().start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Determines whether to render a single focused group or the full 12-group grid.
     */
    private Pane buildCenterContent() {
        if (trackedTeamName != null) {
            // Locate the group containing the tracked team
            Group focusGroup = findGroupForTeam(trackedTeamName);
            if (focusGroup != null) {
                VBox focusedBox = new VBox(15);
                focusedBox.setAlignment(Pos.CENTER);
                focusedBox.setPadding(new Insets(30));

                Label focusLabel = new Label("Focus View: " + trackedTeamName + "'s Group");
                focusLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");

                VBox groupCard = createGroupTableCard(focusGroup);
                groupCard.setMaxWidth(600); // Give the single group card prominent width

                focusedBox.getChildren().addAll(focusLabel, groupCard);
                return focusedBox;
            }
        }

        // Fallback: Default Birds-Eye view (All 12 Groups)
        return buildAllGroupsGrid();
    }

    private GridPane buildAllGroupsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        int col = 0, row = 0;
        for (Group group : tournamentManager.getGroups()) {
            VBox groupCard = createGroupTableCard(group);
            grid.add(groupCard, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
        return grid;
    }

    /**
     * Pops open a secondary JavaFX Window displaying all 12 Groups.
     */
    private void openAllGroupsWindow() {
        Stage secondaryStage = new Stage();
        secondaryStage.initModality(Modality.NONE); // Allows user to interact with both windows
        secondaryStage.setTitle("2026 FIFA World Cup - All Group Standings");

        ScrollPane scrollPane = new ScrollPane(buildAllGroupsGrid());
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 1000, 700);
        secondaryStage.setScene(scene);
        secondaryStage.show();
    }

    private Group findGroupForTeam(String countryName) {
        for (Group g : tournamentManager.getGroups()) {
            for (Team t : g.getTeams()) {
                if (t.getCountryName().equalsIgnoreCase(countryName)) {
                    return g;
                }
            }
        }
        return null;
    }

    private VBox createGroupTableCard(Group group) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #cbd5e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label groupTitle = new Label("Group " + group.getName());
        groupTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2d3748;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewMatchesBtn = new Button("Results");
        viewMatchesBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #edf2f7; -fx-border-color: #cbd5e0; -fx-border-radius: 3;");
        viewMatchesBtn.setOnAction(e -> showMatchResultsDialog(group));

        header.getChildren().addAll(groupTitle, spacer, viewMatchesBtn);

        TableView<Team> table = new TableView<>();
        table.setPrefHeight(150);

        // 1. Team Name Column
        TableColumn<Team, String> nameCol = new TableColumn<>("Team");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("countryName"));

        // 2. Points (PTS) Column
        TableColumn<Team, Integer> ptsCol = new TableColumn<>("PTS");
        ptsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        // 3. Goals For (GF) Column
        TableColumn<Team, Integer> gfCol = new TableColumn<>("GF");
        gfCol.setCellValueFactory(new PropertyValueFactory<>("goalsFor"));

        // 4. Goals Against (GA) Column
        TableColumn<Team, Integer> gaCol = new TableColumn<>("GA");
        gaCol.setCellValueFactory(new PropertyValueFactory<>("goalsAgainst"));

        // 5. Goal Difference (GD) Column
        TableColumn<Team, Integer> gdCol = new TableColumn<>("GD");
        gdCol.setCellValueFactory(new PropertyValueFactory<>("goalDifference"));

        table.getColumns().addAll(nameCol, ptsCol, gfCol, gaCol, gdCol);

        ObservableList<Team> teamData = FXCollections.observableArrayList(group.getTeams());
        table.setItems(teamData);

        box.getChildren().addAll(header, table);
        return box;
    }

    private void showMatchResultsDialog(Group group) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Group " + group.getName() + " - Match Results");
        alert.setHeaderText("Fixtures & Scores for Group " + group.getName());

        VBox content = new VBox(8);
        content.setPadding(new Insets(10));

        if (group.getMatches().isEmpty()) {
            content.getChildren().add(new Label("No matches played yet. Click 'Simulate All Group Matches' first!"));
        } else {
            for (Match m : group.getMatches()) {
                String line = String.format("%s %d - %d %s  (%s | %s)", 
                    m.getTeamA().getCountryName(), m.getScoreA(),
                    m.getScoreB(), m.getTeamB().getCountryName(),
                    m.getLocation(), m.getKickoffTime());
                
                Label matchLabel = new Label(line);
                matchLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
                content.getChildren().add(matchLabel);
            }
        }

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}