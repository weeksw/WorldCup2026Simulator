package application;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class KnockoutStageView {

    private Stage primaryStage;
    private TournamentManager tournamentManager;
    private String selectedMode;
    private Scene groupStageScene;

    private BorderPane mainLayout;
    private ScrollPane listViewPane;
    private ScrollPane bracketViewPane;
    private VBox matchesContainer;

    private Button simStageButton;
    private Button nextStageButton;
    private Button prevStageButton;
    private Label titleLabel;

    private final String[] STAGE_NAMES = {"Round of 32", "Round of 16", "Quarter-Finals", "Semi-Finals", "Finals"};
    private int activeViewStageIndex = 0;

    public KnockoutStageView(Stage primaryStage, TournamentManager tournamentManager, String selectedMode, Scene groupStageScene) {
        this.primaryStage = primaryStage;
        this.tournamentManager = tournamentManager;
        this.selectedMode = selectedMode;
        this.groupStageScene = groupStageScene;
        this.activeViewStageIndex = tournamentManager.getCurrentKnockoutStageIndex();
    }

    public Scene createKnockoutScene() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));

        Button backToGroupBtn = new Button("← Back to Group Stage");
        backToGroupBtn.setStyle("-fx-background-color: #cbd5e0; -fx-font-weight: bold; -fx-font-size: 13px;");
        backToGroupBtn.setOnAction(e -> primaryStage.setScene(groupStageScene));

        prevStageButton = new Button("◄ Prev Stage");
        prevStageButton.setStyle("-fx-background-color: #e2e8f0; -fx-font-weight: bold; -fx-font-size: 13px;");
        prevStageButton.setOnAction(e -> navigateStage(-1));

        titleLabel = new Label(STAGE_NAMES[activeViewStageIndex]);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        simStageButton = new Button("Simulate Round ⚡");
        simStageButton.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        nextStageButton = new Button("Next Stage ►");
        nextStageButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        ToggleButton listToggle = new ToggleButton("List View");
        ToggleButton bracketToggle = new ToggleButton("Bracket View");
        ToggleGroup viewGroup = new ToggleGroup();
        listToggle.setToggleGroup(viewGroup);
        bracketToggle.setToggleGroup(viewGroup);
        listToggle.setSelected(true);

        HBox viewToggleBox = new HBox(listToggle, bracketToggle);
        viewToggleBox.setAlignment(Pos.CENTER);

        HBox topBar = new HBox(12, backToGroupBtn, prevStageButton, titleLabel, simStageButton, nextStageButton, viewToggleBox);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        mainLayout.setTop(topBar);

        matchesContainer = new VBox(10);
        matchesContainer.setPadding(new Insets(10));
        listViewPane = new ScrollPane(matchesContainer);
        listViewPane.setFitToWidth(true);

        bracketViewPane = new ScrollPane();
        bracketViewPane.setFitToWidth(false);
        bracketViewPane.setFitToHeight(true);

        mainLayout.setCenter(listViewPane);

        listToggle.setOnAction(e -> mainLayout.setCenter(listViewPane));
        bracketToggle.setOnAction(e -> {
            bracketViewPane.setContent(buildFullProgressionBracketView());
            mainLayout.setCenter(bracketViewPane);
        });

        simStageButton.setOnAction(e -> simulateCurrentStage());
        nextStageButton.setOnAction(e -> navigateStage(1));

        updateStageUIState();

        return new Scene(mainLayout, 1150, 750);
    }

    private void navigateStage(int direction) {
        int targetIndex = activeViewStageIndex + direction;
        if (targetIndex >= 0 && targetIndex <= tournamentManager.getCurrentKnockoutStageIndex()) {
            activeViewStageIndex = targetIndex;
            updateStageUIState();
        } else if (targetIndex == tournamentManager.getCurrentKnockoutStageIndex() + 1 && isCurrentStageFullyPlayed()) {
            advanceTournamentToNextRound();
        }
    }

    private void advanceTournamentToNextRound() {
        int highest = tournamentManager.getCurrentKnockoutStageIndex();
        if (highest == 0) tournamentManager.setupRoundOf16();
        else if (highest == 1) tournamentManager.setupQuarterFinals();
        else if (highest == 2) tournamentManager.setupSemiFinals();
        else if (highest == 3) tournamentManager.setupFinals();
        else if (highest == 4) {
            showChampionScreen(tournamentManager.getFinalMatch().getWinner());
            return;
        }

        tournamentManager.setCurrentKnockoutStageIndex(highest + 1);
        activeViewStageIndex = highest + 1;
        updateStageUIState();
    }

    private void simulateCurrentStage() {
        List<Match> matches = getCurrentStageMatches(activeViewStageIndex);
        boolean userTeamWasEliminatedThisRound = false;
        String userTeam = tournamentManager.getUserChosenTeamName();

        for (Match m : matches) {
            if (!m.isPlayed()) {
                m.playMatch();
                if (tournamentManager.isUserTeamEliminated(m)) {
                    userTeamWasEliminatedThisRound = true;
                }
            }
        }

        updateStageUIState();

        if (userTeamWasEliminatedThisRound) {
            promptUserTeamEliminated(userTeam);
        }
    }

    private void updateStageUIState() {
        titleLabel.setText(STAGE_NAMES[activeViewStageIndex]);
        List<Match> currentMatches = getCurrentStageMatches(activeViewStageIndex);

        renderListView(currentMatches);
        bracketViewPane.setContent(buildFullProgressionBracketView());

        boolean stagePlayed = isStagePlayed(activeViewStageIndex);

        prevStageButton.setDisable(activeViewStageIndex == 0);
        simStageButton.setDisable(stagePlayed);

        if (stagePlayed) {
            simStageButton.setText("Completed ✓");
            nextStageButton.setDisable(false);
            if (activeViewStageIndex == 4) {
                nextStageButton.setText("Crown Champion 🎉");
            } else {
                nextStageButton.setText("Next Stage ►");
            }
        } else {
            simStageButton.setText("Simulate " + STAGE_NAMES[activeViewStageIndex] + " ⚡");
            nextStageButton.setDisable(true);
        }
    }

    private boolean isStagePlayed(int stageIndex) {
        List<Match> matches = getCurrentStageMatches(stageIndex);
        if (matches.isEmpty()) return false;
        for (Match m : matches) {
            if (!m.isPlayed()) return false;
        }
        return true;
    }

    private boolean isCurrentStageFullyPlayed() {
        return isStagePlayed(tournamentManager.getCurrentKnockoutStageIndex());
    }

    private List<Match> getCurrentStageMatches(int stageIndex) {
        switch (stageIndex) {
            case 0: 
                List<Match> ro32 = tournamentManager.getKnockoutRound32Matches();
                if (ro32 == null || ro32.isEmpty()) {
                    ro32 = tournamentManager.setupRoundOf32();
                }
                return ro32;
            case 1: return tournamentManager.getKnockoutRound16Matches();
            case 2: return tournamentManager.getQuarterFinalMatches();
            case 3: return tournamentManager.getSemiFinalMatches();
            case 4: 
                List<Match> finals = new ArrayList<>();
                if (tournamentManager.getFinalMatch() != null) finals.add(tournamentManager.getFinalMatch());
                if (tournamentManager.getThirdPlaceMatch() != null) finals.add(tournamentManager.getThirdPlaceMatch());
                return finals;
            default: return new ArrayList<>();
        }
    }

    private void renderListView(List<Match> matches) {
        matchesContainer.getChildren().clear();
        String focusTeam = tournamentManager.getUserChosenTeamName();

        if (activeViewStageIndex == 4 && !matches.isEmpty()) {
            for (Match m : matches) {
                boolean isFinal = (m == tournamentManager.getFinalMatch());
                Label headerLabel = new Label(isFinal ? "🏆 WORLD CUP FINAL" : "🥉 3RD PLACE PLAY-OFF");
                headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2b6cb0; -fx-padding: 10 0 5 0;");
                matchesContainer.getChildren().add(headerLabel);

                HBox card = createMatchListCard(m, focusTeam, isFinal);
                matchesContainer.getChildren().add(card);
            }
        } else {
            for (Match m : matches) {
                HBox card = createMatchListCard(m, focusTeam, false);
                matchesContainer.getChildren().add(card);
            }
        }
    }

    private HBox createMatchListCard(Match m, String focusTeam, boolean isFinal) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);

        boolean isFocusMatch = focusTeam != null && !focusTeam.isEmpty() &&
            (m.getTeamA().getCountryName().equalsIgnoreCase(focusTeam) || m.getTeamB().getCountryName().equalsIgnoreCase(focusTeam));

        String baseStyle = "-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";
        if (isFocusMatch) {
            card.setStyle(baseStyle + " -fx-border-color: #ecc94b; -fx-border-width: 2.5;");
        } else {
            card.setStyle(baseStyle + " -fx-border-color: #e2e8f0;");
        }

        Label locationLabel = new Label(m.getLocation() + " | " + m.getKickoffTime());
        locationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096; -fx-pref-width: 170;");

        Label teamA = new Label(m.getTeamA().getCountryName());
        teamA.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-pref-width: 140; -fx-alignment: CENTER-RIGHT;");

        Label teamB = new Label(m.getTeamB().getCountryName());
        teamB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-pref-width: 140;");

        if (m.isPlayed() && activeViewStageIndex == 4) {
            Team winner = m.getWinner();
            Team loser = (winner == m.getTeamA()) ? m.getTeamB() : m.getTeamA();

            if (isFinal) {
                if (m.getTeamA() == winner) teamA.setText("🥇 " + m.getTeamA().getCountryName() + " (CHAMPION)");
                if (m.getTeamB() == winner) teamB.setText("🥇 " + m.getTeamB().getCountryName() + " (CHAMPION)");
                if (m.getTeamA() == loser) teamA.setText("🥈 " + m.getTeamA().getCountryName() + " (RUNNER-UP)");
                if (m.getTeamB() == loser) teamB.setText("🥈 " + m.getTeamB().getCountryName() + " (RUNNER-UP)");
            } else {
                if (m.getTeamA() == winner) teamA.setText("🥉 " + m.getTeamA().getCountryName() + " (3RD PLACE)");
                if (m.getTeamB() == winner) teamB.setText("🥉 " + m.getTeamB().getCountryName() + " (3RD PLACE)");
            }
        }

        String scoreText = m.isPlayed() ? (m.getScoreA() + " - " + m.getScoreB()) : "VS";
        Label score = new Label(scoreText);
        score.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: #edf2f7; -fx-padding: 4 10; -fx-background-radius: 4;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        card.getChildren().addAll(locationLabel, spacer1, teamA, score, teamB, spacer2);
        return card;
    }

    /**
     * Builds Bracket View with precise drawing overlay.
     */
    private Pane buildFullProgressionBracketView() {
        HBox bracketTree = new HBox(80); // Comfortable gap for connecting lines
        bracketTree.setPadding(new Insets(20));
        bracketTree.setAlignment(Pos.CENTER_LEFT);

        int maxUnlocked = tournamentManager.getCurrentKnockoutStageIndex();
        List<List<VBox>> allMatchNodesByStage = new ArrayList<>();

        for (int stageIdx = 0; stageIdx <= maxUnlocked; stageIdx++) {
            VBox col = new VBox(24);
            col.setAlignment(Pos.CENTER);

            Label colTitle = new Label(STAGE_NAMES[stageIdx]);
            colTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2b6cb0;");
            col.getChildren().add(colTitle);

            List<Match> stageMatches = getCurrentStageMatches(stageIdx);
            List<VBox> currentStageNodes = new ArrayList<>();

            if (stageIdx == 4) { 
                Match finalMatch = tournamentManager.getFinalMatch();
                Match thirdPlace = tournamentManager.getThirdPlaceMatch();

                if (finalMatch != null) {
                    Label finalHeader = new Label("🏆 WORLD CUP FINAL");
                    finalHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #d69e2e;");
                    col.getChildren().add(finalHeader);

                    VBox finalNode = createBracketMatchNode(finalMatch, true, false);
                    col.getChildren().add(finalNode);
                    currentStageNodes.add(finalNode);
                }

                if (thirdPlace != null) {
                    Label thirdHeader = new Label("🥉 3RD PLACE MATCH");
                    thirdHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #b7791f;");
                    col.getChildren().add(thirdHeader);

                    VBox thirdNode = createBracketMatchNode(thirdPlace, false, true);
                    col.getChildren().add(thirdNode);
                }
            } else {
                for (Match m : stageMatches) {
                    VBox matchNode = createBracketMatchNode(m, false, false);
                    col.getChildren().add(matchNode);
                    currentStageNodes.add(matchNode);
                }
            }

            allMatchNodesByStage.add(currentStageNodes);
            bracketTree.getChildren().add(col);
        }

        Pane overlayPane = new Pane();
        overlayPane.setMouseTransparent(true);

     // Use StackPane so overlayPane sits directly on top of bracketTree,
        // and it correctly returns a Pane.
        StackPane canvasContainer = new StackPane(bracketTree, overlayPane);
        canvasContainer.setAlignment(Pos.CENTER_LEFT);

        // Re-draw bracket lines dynamically whenever the UI updates layout
        bracketTree.boundsInParentProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> drawBracketConnectors(allMatchNodesByStage, overlayPane));
        });

        return canvasContainer;
    }

    private void drawBracketConnectors(List<List<VBox>> stageNodes, Pane overlayPane) {
        overlayPane.getChildren().clear();

        for (int stage = 0; stage < stageNodes.size() - 1; stage++) {
            List<VBox> currentRound = stageNodes.get(stage);
            List<VBox> nextRound = stageNodes.get(stage + 1);

            if (nextRound.isEmpty()) continue;

            for (int i = 0; i < currentRound.size(); i++) {
                VBox sourceNode = currentRound.get(i);
                int targetIndex = i / 2;

                if (targetIndex < nextRound.size()) {
                    VBox targetNode = nextRound.get(targetIndex);

                    // Compute precise relative coordinates
                    Point2D startScene = sourceNode.localToScene(sourceNode.getWidth(), sourceNode.getHeight() / 2.0);
                    Point2D endScene = targetNode.localToScene(0, targetNode.getHeight() / 2.0);

                    if (startScene == null || endScene == null) continue;

                    Point2D start = overlayPane.sceneToLocal(startScene);
                    Point2D end = overlayPane.sceneToLocal(endScene);

                    if (start == null || end == null) continue;

                    double startX = start.getX();
                    double startY = start.getY();
                    double endX = end.getX();
                    double endY = end.getY();

                    double midX = startX + (endX - startX) / 2.0;

                    // Orthogonal elbow lines like ESPN/Google World Cup brackets
                    Line line1 = new Line(startX, startY, midX, startY);
                    Line line2 = new Line(midX, startY, midX, endY);
                    Line line3 = new Line(midX, endY, endX, endY);

                    for (Line l : new Line[]{line1, line2, line3}) {
                        l.setStroke(Color.web("#a0aec0"));
                        l.setStrokeWidth(2);
                        overlayPane.getChildren().add(l);
                    }
                }
            }
        }
    }

    private VBox createBracketMatchNode(Match m, boolean isFinalMatch, boolean isThirdPlaceMatch) {
        VBox box = new VBox(4);
        String focusTeam = tournamentManager.getUserChosenTeamName();

        boolean isFocusMatch = focusTeam != null && !focusTeam.isEmpty() &&
            (m.getTeamA().getCountryName().equalsIgnoreCase(focusTeam) || m.getTeamB().getCountryName().equalsIgnoreCase(focusTeam));

        String borderStyle = isFocusMatch ? "-fx-border-color: #d69e2e; -fx-border-width: 2;" : "-fx-border-color: #cbd5e0;";
        box.setStyle("-fx-background-color: white; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8; -fx-pref-width: 180; " + borderStyle);

        Label labelA = new Label(m.getTeamA().getCountryName() + (m.isPlayed() ? " (" + m.getScoreA() + ")" : ""));
        Label labelB = new Label(m.getTeamB().getCountryName() + (m.isPlayed() ? " (" + m.getScoreB() + ")" : ""));

        labelA.setStyle("-fx-font-size: 12px;");
        labelB.setStyle("-fx-font-size: 12px;");

        if (m.isPlayed()) {
            Team winner = m.getWinner();

            if (isFinalMatch) {
                if (m.getTeamA() == winner) {
                    labelA.setText("🥇 " + m.getTeamA().getCountryName() + " (" + m.getScoreA() + ")");
                    labelA.setStyle("-fx-font-weight: bold; -fx-text-fill: #b7791f; -fx-font-size: 12px;");
                } else {
                    labelA.setText("🥈 " + m.getTeamA().getCountryName() + " (" + m.getScoreA() + ")");
                    labelA.setStyle("-fx-font-weight: bold; -fx-text-fill: #718096; -fx-font-size: 12px;");
                }

                if (m.getTeamB() == winner) {
                    labelB.setText("🥇 " + m.getTeamB().getCountryName() + " (" + m.getScoreB() + ")");
                    labelB.setStyle("-fx-font-weight: bold; -fx-text-fill: #b7791f; -fx-font-size: 12px;");
                } else {
                    labelB.setText("🥈 " + m.getTeamB().getCountryName() + " (" + m.getScoreB() + ")");
                    labelB.setStyle("-fx-font-weight: bold; -fx-text-fill: #718096; -fx-font-size: 12px;");
                }
            } else if (isThirdPlaceMatch) {
                if (m.getTeamA() == winner) {
                    labelA.setText("🥉 " + m.getTeamA().getCountryName() + " (" + m.getScoreA() + ")");
                    labelA.setStyle("-fx-font-weight: bold; -fx-text-fill: #dd6b20; -fx-font-size: 12px;");
                }
                if (m.getTeamB() == winner) {
                    labelB.setText("🥉 " + m.getTeamB().getCountryName() + " (" + m.getScoreB() + ")");
                    labelB.setStyle("-fx-font-weight: bold; -fx-text-fill: #dd6b20; -fx-font-size: 12px;");
                }
            } else {
                if (m.getWinner() == m.getTeamA()) labelA.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b6cb0; -fx-font-size: 12px;");
                else if (m.getWinner() == m.getTeamB()) labelB.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b6cb0; -fx-font-size: 12px;");
            }
        }

        box.getChildren().addAll(labelA, new Separator(), labelB);
        return box;
    }

    private void promptUserTeamEliminated(String teamName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Team Eliminated");
        alert.setHeaderText("❌ " + teamName + " Has Been Knocked Out!");
        alert.setContentText(teamName + " lost their knockout match. Would you like to view the remainder of the tournament or start over?");

        ButtonType viewRemainderBtn = new ButtonType("View Remainder");
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

    private void showChampionScreen(Team champion) {
        VBox champLayout = new VBox(20);
        champLayout.setAlignment(Pos.CENTER);
        champLayout.setPadding(new Insets(40));
        champLayout.setStyle("-fx-background-color: #1a202c;");

        Label trophyHeader = new Label("🏆 2026 FIFA WORLD CUP CHAMPIONS 🏆");
        trophyHeader.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f6e05e;");

        Label teamName = new Label(champion.getCountryName().toUpperCase());
        teamName.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label congrats = new Label("Congratulations! " + champion.getCountryName() + " has won the World Cup!");
        congrats.setStyle("-fx-font-size: 18px; -fx-text-fill: #cbd5e0;");

        Button restartBtn = new Button("🔄 Start New Tournament");
        restartBtn.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 20;");
        restartBtn.setOnAction(e -> {
            try {
                new Main().start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        champLayout.getChildren().addAll(trophyHeader, teamName, congrats, restartBtn);
        primaryStage.setScene(new Scene(champLayout, 1150, 750));
    }
}