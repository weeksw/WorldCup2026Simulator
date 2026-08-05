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
    private VBox matchesContainer;

    public KnockoutStageView(Stage primaryStage, TournamentManager tournamentManager, String selectedMode) {
        this.primaryStage = primaryStage;
        this.tournamentManager = tournamentManager;
        this.selectedMode = selectedMode;
    }

    public Scene createKnockoutScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Top Navigation Bar
        Label titleLabel = new Label("Round of 32 - Knockout Phase");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");

        Button simRO32Button = new Button("Simulate Round of 32 ⚡");
        simRO32Button.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox topBar = new HBox(20, titleLabel, simRO32Button);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));
        root.setTop(topBar);

        // Populate RO32 Fixtures
        List<Match> ro32Matches = tournamentManager.setupRoundOf32();

        matchesContainer = new VBox(12);
        matchesContainer.setPadding(new Insets(10));
        renderMatches(ro32Matches);

        ScrollPane scrollPane = new ScrollPane(matchesContainer);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        // Simulation Event
        simRO32Button.setOnAction(e -> {
            for (Match m : ro32Matches) {
                m.playMatch();
            }
            renderMatches(ro32Matches); // Update scores visually
            simRO32Button.setDisable(true);
        });

        return new Scene(root, 1000, 700);
    }

    private void renderMatches(List<Match> matches) {
        matchesContainer.getChildren().clear();

        for (Match m : matches) {
            HBox card = new HBox(15);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");

            Label locationLabel = new Label(m.getLocation() + " | " + m.getKickoffTime());
            locationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096; -fx-pref-width: 180;");

            Label teamA = new Label(m.getTeamA().getCountryName());
            teamA.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-pref-width: 150; -fx-alignment: CENTER-RIGHT;");

            Label score = new Label(m.getScoreA() + " - " + m.getScoreB());
            score.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-background-color: #edf2f7; -fx-padding: 4 12; -fx-background-radius: 4;");

            Label teamB = new Label(m.getTeamB().getCountryName());
            teamB.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-pref-width: 150;");

            // First spacer
            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);

            // Second unique spacer
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            // Added distinct spacers so no duplicates exist
            card.getChildren().addAll(locationLabel, spacer1, teamA, score, teamB, spacer2);
            matchesContainer.getChildren().add(card);
        }
    }
}