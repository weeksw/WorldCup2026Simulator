package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GroupStageView {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private String selectedMode;

    public GroupStageView(Stage primaryStage, TournamentManager tournamentManager, String selectedMode) {
        this.primaryStage = primaryStage;
        this.tournamentManager = tournamentManager;
        this.selectedMode = selectedMode;
    }

    public Scene createGroupStageScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top Control Bar
        Label titleLabel = new Label("Group Stage - 2026 FIFA World Cup (" + selectedMode + ")");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        Button simButton = new Button("Simulate All Group Matches");
        simButton.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button nextStageButton = new Button("Advance to Knockout Stage ->");
        nextStageButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nextStageButton.setDisable(true); // Locked until matches are played

        HBox topBar = new HBox(20, titleLabel, simButton, nextStageButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBar);

        // Center Grid for 12 Groups (3 rows x 4 columns)
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Create a TableView for each of the 12 groups
        int col = 0;
        int row = 0;
        for (Group group : tournamentManager.getGroups()) {
            VBox groupCard = createGroupTableCard(group);
            grid.add(groupCard, col, row);

            col++;
            if (col == 4) { // Move to next row after 4 columns
                col = 0;
                row++;
            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // Simulation Action Logic
        simButton.setOnAction(e -> {
            tournamentManager.runGroupStage();
            
            // Refresh the tables visually
            scrollPane.setContent(rebuildGrid());
            simButton.setDisable(true);
            nextStageButton.setDisable(false);
        });

        return new Scene(root, 1100, 750);
    }

    private GridPane rebuildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
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

    private VBox createGroupTableCard(Group group) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #cbd5e0; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 8;");

        // Header HBox with Title and "View Matches" button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label groupTitle = new Label("Group " + group.getName());
        groupTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2d3748;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewMatchesBtn = new Button("Results");
        viewMatchesBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #edf2f7; -fx-border-color: #cbd5e0; -fx-border-radius: 3;");
        
        // Popup window showing all match scores for this group
        viewMatchesBtn.setOnAction(e -> showMatchResultsDialog(group));

        header.getChildren().addAll(groupTitle, spacer, viewMatchesBtn);

        TableView<Team> table = new TableView<>();
        table.setPrefHeight(145);

        // Columns
        TableColumn<Team, String> nameCol = new TableColumn<>("Team");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("countryName"));

        TableColumn<Team, Integer> ptsCol = new TableColumn<>("PTS");
        ptsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        TableColumn<Team, Integer> gdCol = new TableColumn<>("GD");
        gdCol.setCellValueFactory(new PropertyValueFactory<>("goalDifference"));

        TableColumn<Team, Integer> gfCol = new TableColumn<>("GF");
        gfCol.setCellValueFactory(new PropertyValueFactory<>("goalsFor"));

        // ADDED: Goals Against Column
        TableColumn<Team, Integer> gaCol = new TableColumn<>("GA");
        gaCol.setCellValueFactory(new PropertyValueFactory<>("goalsAgainst"));

        table.getColumns().addAll(nameCol, ptsCol, gdCol, gfCol, gaCol);

        ObservableList<Team> teamData = FXCollections.observableArrayList(group.getTeams());
        table.setItems(teamData);

        box.getChildren().addAll(header, table);
        return box;
    }

    // Helper method to display match scores in a modal dialog
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