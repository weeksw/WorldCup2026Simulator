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

/**
 * User interface dashboard for the Group Stage of the Tournament Engine.
 * <p>
 * Renders standings for all 12 groups, supports dual-view modes (Focused Team vs. Spectate),
 * handles match simulation execution, and enforces user team qualification/elimination checks.
 * </p>
 * 
 * @author William Weeks
 * @version 1.0
 */

public class GroupStageView {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private String selectedMode;
    private String trackedTeamName;
    private ScrollPane centerContainer;
    private Button simButton;
    private Button nextStageButton;
    
    /**
     * Constructs the GroupStageView dashboard and parses mode selection.
     * 
     * @param primaryStage The primary JavaFX window frame for scene switching.
     * @param tournamentManager The core engine handling team, group, and match data.
     * @param selectedMode Operating mode string ("Spectate" or "Tracking: [Country]").
     */
    
    public GroupStageView(Stage primaryStage, TournamentManager tournamentManager, String selectedMode) {
    	
        this.primaryStage = primaryStage;
        this.tournamentManager = tournamentManager;
        this.selectedMode = selectedMode;

        // Parse team tracking context from incoming mode flag
        if (selectedMode != null && selectedMode.startsWith("Tracking: ")) {
            this.trackedTeamName = selectedMode.replace("Tracking: ", "").trim();
            this.tournamentManager.setUserChosenTeamName(this.trackedTeamName);
        }
    }

    /**
     * Checks if the tracked user team qualified for the knockout stage.
     * <p>
     * Prompts an alert dialog if the team was eliminated in group play, offering
     * the option to transition to spectate mode or restart the simulator.
     * </p>
     */
    
    private void checkUserTeamElimination() {
    	
        String userTeam = tournamentManager.getUserChosenTeamName();
        
        // Skip validation if spectating or if no valid team is tracked
        if ("Spectate".equalsIgnoreCase(tournamentManager.getMode()) 
                || userTeam == null 
                || userTeam.trim().isEmpty() 
                || userTeam.equalsIgnoreCase("Default")
                || userTeam.equalsIgnoreCase("Select a Team...")) {
            return;
        }

        // Query backend manager to confirm knockout qualification
        boolean qualified = tournamentManager.didTeamQualifyForKnockout(userTeam);

        if (!qualified) {
        	
            promptUserTeamEliminated(userTeam);
        }
    }
    
    /**
     * Builds and configures the core JavaFX Scene for the Group Stage view.
     * 
     * @return Scene The rendered Group Stage interface.
     */
    
    @SuppressWarnings(value = {"unused"})
    public Scene createGroupStageScene() {
    	
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top Navigation Header
        Label titleLabel = new Label("Group Stage - 2026 FIFA World Cup");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        simButton = new Button("Simulate All Group Matches");
        simButton.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        nextStageButton = new Button("Advance to Knockout Stage ->");
        nextStageButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nextStageButton.setDisable(true); // Locked until group stage matches execute

        // Navigation to Knockout Stage
        nextStageButton.setOnAction(e -> {
            KnockoutStageView knockoutView = new KnockoutStageView(primaryStage, tournamentManager, selectedMode, primaryStage.getScene());
            primaryStage.setScene(knockoutView.createKnockoutScene());
        });
        
        HBox topBar = new HBox(15, titleLabel, simButton, nextStageButton);

        // Render external window control when operating in focused tracking mode
        if (trackedTeamName != null) {
        	
            Button viewAllBtn = new Button("Explore All Groups 🌐");
            viewAllBtn.setStyle("-fx-background-color: #718096; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            viewAllBtn.setOnAction(e -> openAllGroupsWindow());
            topBar.getChildren().add(viewAllBtn);
        }

        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBar);

        // Center Content Container
        centerContainer = new ScrollPane();
        centerContainer.setFitToWidth(true);
        centerContainer.setContent(buildCenterContent());
        root.setCenter(centerContainer);

        // Restore UI state if group stage was previously executed
        if (isGroupStageAlreadySimulated()) {
        	
            simButton.setDisable(true);
            simButton.setText("Completed ✓");
            nextStageButton.setDisable(false);
        }

        // Action Handler: Executes group stage simulation algorithm
        simButton.setOnAction(e -> {
            tournamentManager.runGroupStage();
            centerContainer.setContent(buildCenterContent()); // Re-render tables with updated match results
            simButton.setDisable(true);
            simButton.setText("Completed ✓");
            nextStageButton.setDisable(false);

            // Trigger elimination check for tracked team
            checkUserTeamElimination();
        });

        return new Scene(root, 1100, 750);
    }

    /**
     * Determines whether group stage match results have already been processed in the engine.
     * 
     * @return boolean True if at least one group match has been marked as played.
     */
    
    private boolean isGroupStageAlreadySimulated() {
    	
        for (Group g : tournamentManager.getGroups()) {
        	
            for (Match m : g.getMatches()) {
            	
                if (m.isPlayed()) return true;
            }
        }
        return false;
    }

    /**
     * Displays a decision dialog when a user's tracked team is eliminated in group play.
     * 
     * @param teamName The nation that was eliminated.
     */
    
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
                    new Main().start(primaryStage); // Restart application state
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Evaluates view context and constructs either a single focused group card or the complete grid.
     * 
     * @return Pane The primary layout node containing group table view(s).
     */
    
    private Pane buildCenterContent() {
    	
        if (trackedTeamName != null) {
        	
            Group focusGroup = findGroupForTeam(trackedTeamName);
            if (focusGroup != null) {
            	
                VBox focusedBox = new VBox(15);
                focusedBox.setAlignment(Pos.CENTER);
                focusedBox.setPadding(new Insets(30));

                Label focusLabel = new Label("Focus View: " + trackedTeamName + "'s Group");
                focusLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2b6cb0;");

                VBox groupCard = createGroupTableCard(focusGroup);
                groupCard.setMaxWidth(600);

                focusedBox.getChildren().addAll(focusLabel, groupCard);
                return focusedBox;
            }
        }

        // Fallback or default view: Full 12-group grid layout
        return buildAllGroupsGrid();
    }

    /**
     * Assembles a 4-column responsive GridPane holding all 12 tournament group cards.
     * 
     * @return GridPane The rendered grid matrix.
     */
    
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
            	// Row break after 4 columns
                col = 0;
                row++;
            }
        }
        return grid;
    }

    /**
     * Opens a non-modal secondary window displaying all 12 group tables concurrently.
     */
    
    private void openAllGroupsWindow() {
    	
        Stage secondaryStage = new Stage();
        secondaryStage.initModality(Modality.NONE);
        secondaryStage.setTitle("2026 FIFA World Cup - All Group Standings");

        ScrollPane scrollPane = new ScrollPane(buildAllGroupsGrid());
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 1000, 700);
        secondaryStage.setScene(scene);
        secondaryStage.show();
    }

    /**
     * Helper lookup utility to identify which group contains a specific team.
     * 
     * @param countryName Name of the team to locate.
     * @return Group The matching group instance, or null if not found.
     */
    
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

    /**
     * Generates an individual UI card containing a formatted JavaFX TableView of group standings.
     * 
     * @param group The group object holding standing metrics (PTS, GF, GA, GD).
     * @return VBox The visual group table component.
     */
    
    @SuppressWarnings({ "unchecked", "unused" })
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

        // Configure JavaFX TableView and column mappings
        TableView<Team> table = new TableView<>();
        table.setPrefHeight(150);

        TableColumn<Team, String> nameCol = new TableColumn<>("Team");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("countryName"));

        TableColumn<Team, Integer> ptsCol = new TableColumn<>("PTS");
        ptsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        TableColumn<Team, Integer> gfCol = new TableColumn<>("GF");
        gfCol.setCellValueFactory(new PropertyValueFactory<>("goalsFor"));

        TableColumn<Team, Integer> gaCol = new TableColumn<>("GA");
        gaCol.setCellValueFactory(new PropertyValueFactory<>("goalsAgainst"));

        TableColumn<Team, Integer> gdCol = new TableColumn<>("GD");
        gdCol.setCellValueFactory(new PropertyValueFactory<>("goalDifference"));

        table.getColumns().addAll(nameCol, ptsCol, gfCol, gaCol, gdCol);

        ObservableList<Team> teamData = FXCollections.observableArrayList(group.getTeams());
        table.setItems(teamData);

        box.getChildren().addAll(header, table);
        return box;
    }

    /**
     * Displays an informational modal with specific scorelines, venues, and kickoff times.
     * 
     * @param group The group whose fixture results will be rendered.
     */
    
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