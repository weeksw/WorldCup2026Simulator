package application;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class KnockoutStageView {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private String selectedMode;
    private Scene groupStageScene; // Reference to navigate back

    private BorderPane mainLayout;
    private ScrollPane listViewPane;
    private ScrollPane bracketViewPane;
    private VBox matchesContainer;

    private Button simRO32Button;
    private Button nextStageButton;

    public KnockoutStageView(Stage primaryStage, TournamentManager tournamentManager, String selectedMode, Scene groupStageScene) {
        this.primaryStage = primaryStage;
        this.tournamentManager = tournamentManager;
        this.selectedMode = selectedMode;
        this.groupStageScene = groupStageScene;
    }

    public Scene createKnockoutScene() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));

        // --- TOP NAVIGATION BAR ---
        Button backButton = new Button("← Back to Group Stage");
        backButton.setStyle("-fx-background-color: #cbd5e0; -fx-font-weight: bold; -fx-font-size: 13px;");
        backButton.setOnAction(e -> primaryStage.setScene(groupStageScene));

        Label titleLabel = new Label("Round of 32");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        simRO32Button = new Button("Simulate Round of 32 ⚡");
        simRO32Button.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        nextStageButton = new Button("Advance to Round of 16 →");
        nextStageButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        nextStageButton.setDisable(true); // Enabled after simulation

        // View Toggles (List vs Bracket)
        ToggleButton listToggle = new ToggleButton("List View");
        ToggleButton bracketToggle = new ToggleButton("Bracket View");
        ToggleGroup viewGroup = new ToggleGroup();
        listToggle.setToggleGroup(viewGroup);
        bracketToggle.setToggleGroup(viewGroup);
        listToggle.setSelected(true);

        HBox viewToggleBox = new HBox(listToggle, bracketToggle);
        viewToggleBox.setAlignment(Pos.CENTER);

        HBox topBar = new HBox(15, backButton, titleLabel, simRO32Button, nextStageButton, viewToggleBox);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        mainLayout.setTop(topBar);

        // --- CONTENT PANES ---
        List<Match> ro32Matches = tournamentManager.getKnockoutRound32Matches();
        if (ro32Matches.isEmpty()) {
            ro32Matches = tournamentManager.setupRoundOf32();
        }

        // 1. List View Container
        matchesContainer = new VBox(10);
        matchesContainer.setPadding(new Insets(10));
        renderListView(ro32Matches);
        listViewPane = new ScrollPane(matchesContainer);
        listViewPane.setFitToWidth(true);

        // 2. Bracket View Container
        bracketViewPane = new ScrollPane(buildBracketView(ro32Matches));
        bracketViewPane.setFitToWidth(true);

        // Default content is List View
        mainLayout.setCenter(listViewPane);

        // --- TOGGLE EVENT HANDLERS ---
        listToggle.setOnAction(e -> mainLayout.setCenter(listViewPane));
        bracketToggle.setOnAction(e -> {
            bracketViewPane.setContent(buildBracketView(tournamentManager.getKnockoutRound32Matches()));
            mainLayout.setCenter(bracketViewPane);
        });

        // --- SIMULATION ACTION ---
        simRO32Button.setOnAction(e -> {
            for (Match m : tournamentManager.getKnockoutRound32Matches()) {
                if (!m.isPlayed()) {
                    m.playMatch();
                }
            }
            renderListView(tournamentManager.getKnockoutRound32Matches());
            bracketViewPane.setContent(buildBracketView(tournamentManager.getKnockoutRound32Matches()));
            simRO32Button.setDisable(true);
            nextStageButton.setDisable(false);
        });

        return new Scene(mainLayout, 1150, 750);
    }

    /**
     * Renders the standard list view of match cards.
     */
    private void renderListView(List<Match> matches) {
        matchesContainer.getChildren().clear();

        for (Match m : matches) {
            HBox card = new HBox(15);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");

            Label locationLabel = new Label(m.getLocation() + " | " + m.getKickoffTime());
            locationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096; -fx-pref-width: 170;");

            Label teamA = new Label(m.getTeamA().getCountryName());
            teamA.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-pref-width: 140; -fx-alignment: CENTER-RIGHT;");

            String scoreText = m.isPlayed() ? (m.getScoreA() + " - " + m.getScoreB()) : "VS";
            Label score = new Label(scoreText);
            score.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #edf2f7; -fx-padding: 4 10; -fx-background-radius: 4;");

            Label teamB = new Label(m.getTeamB().getCountryName());
            teamB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-pref-width: 140;");

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            card.getChildren().addAll(locationLabel, spacer1, teamA, score, teamB, spacer2);
            matchesContainer.getChildren().add(card);
        }
    }

    /**
     * Constructs a visual March Madness style Bracket layout with connective nodes.
     */
    private HBox buildBracketView(List<Match> ro32Matches) {
        HBox bracketTree = new HBox(40);
        bracketTree.setPadding(new Insets(20));
        bracketTree.setAlignment(Pos.CENTER_LEFT);

        // Column 1: Round of 32 (16 Matches)
        VBox ro32Col = new VBox(12);
        ro32Col.setAlignment(Pos.CENTER);

        Label col1Title = new Label("Round of 32");
        col1Title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2b6cb0;");
        ro32Col.getChildren().add(col1Title);

        // Column 2: Round of 16 (Preview / Placeholders)
        VBox ro16Col = new VBox(45);
        ro16Col.setAlignment(Pos.CENTER);

        Label col2Title = new Label("Round of 16");
        col2Title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2b6cb0;");
        ro16Col.getChildren().add(col2Title);

        for (int i = 0; i < ro32Matches.size(); i++) {
            Match m = ro32Matches.get(i);
            VBox matchNode = createBracketMatchNode(m);
            ro32Col.getChildren().add(matchNode);

            // Add RO16 projection boxes every 2 RO32 matches
            if (i % 2 == 0) {
                Match mNext = (i + 1 < ro32Matches.size()) ? ro32Matches.get(i + 1) : null;
                VBox futureNode = createRO16ProjectionNode(m, mNext);
                ro16Col.getChildren().add(futureNode);
            }
        }

        bracketTree.getChildren().addAll(ro32Col, ro16Col);
        return bracketTree;
    }

    private VBox createBracketMatchNode(Match m) {
        VBox box = new VBox(2);
        box.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e0; -fx-border-radius: 5; -fx-padding: 6; -fx-pref-width: 180;");

        String nameA = m.getTeamA().getCountryName();
        String nameB = m.getTeamB().getCountryName();

        Label labelA = new Label(nameA + (m.isPlayed() ? " (" + m.getScoreA() + ")" : ""));
        Label labelB = new Label(nameB + (m.isPlayed() ? " (" + m.getScoreB() + ")" : ""));

        labelA.setStyle("-fx-font-size: 12px;");
        labelB.setStyle("-fx-font-size: 12px;");

        if (m.isPlayed()) {
            if (m.getWinner() == m.getTeamA()) {
                labelA.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b6cb0; -fx-font-size: 12px;");
            } else if (m.getWinner() == m.getTeamB()) {
                labelB.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b6cb0; -fx-font-size: 12px;");
            }
        }

        box.getChildren().addAll(labelA, new Separator(), labelB);
        return box;
    }

    private VBox createRO16ProjectionNode(Match m1, Match m2) {
        VBox box = new VBox(2);
        box.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-style: dashed; -fx-border-radius: 5; -fx-padding: 6; -fx-pref-width: 180;");

        String team1Str = (m1 != null && m1.isPlayed()) ? m1.getWinner().getCountryName() : "Winner Match " + (m1 != null ? "A" : "");
        String team2Str = (m2 != null && m2.isPlayed()) ? m2.getWinner().getCountryName() : "Winner Match " + (m2 != null ? "B" : "");

        Label labelA = new Label(team1Str);
        Label labelB = new Label(team2Str);

        labelA.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");
        labelB.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0;");

        if (m1 != null && m1.isPlayed()) labelA.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 11px;");
        if (m2 != null && m2.isPlayed()) labelB.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 11px;");

        box.getChildren().addAll(labelA, new Separator(), labelB);
        return box;
    }
}