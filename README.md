# ⚽ 2026 FIFA World Cup Tournament Simulator & Engine

An interactive, multi-view JavaFX application designed to simulate the expanded **48-team FIFA World Cup 2026** format. The engine models realistic probabilistic match simulations, complex multi-tier group stage tie-breakers, dynamic single-elimination bracket views, and custom nation-tracking modes.

---

## 🌟 Key Features

* **Expanded 48-Team Format:** Faithfully implements 12 four-team groups (Groups A through L) and the new **Round of 32** knockout structure.
* **Dual Rendering Navigation:** 
  * **List View:** Detailed match cards displaying fixtures, kick-off times, and host city venues across North America.
  * **Interactive Bracket View:** Visual tournament tree rendered with custom vector coordinate math and orthogonal elbow connectors.
* **Realistic Match Simulation Engine:** Goal outcomes are dynamically computed using realistic FIFA probability distributions and weighted seeding ranks.
* **FIFA Tie-Breaking Engine:** Group standings tables automatically evaluate **Points $\rightarrow$ Net Goal Difference $\rightarrow$ Total Goals Scored** to determine advancement and select the top 8 third-place wildcards.
* **Custom Operating Modes:**
  * **Spectate Mode:** Neutral overview of the entire tournament.
  * **Tracking Mode:** Highlights a selected nation across group and knockout screens, featuring custom elimination alerts and victory prompts.

---

## 📐 Architecture & UML Class Diagram

The application cleanly decouples presentation layers (`JavaFX Scenes`) from core business logic (`TournamentManager`) and data domain models.

```mermaid
classDiagram
    class Main {
        +start(Stage primaryStage) void
        +main(String[] args) void
    }

    class KnockoutStageView {
        -Stage primaryStage
        -TournamentManager tournamentManager
        -int activeViewStageIndex
        +createKnockoutScene() Scene
        -navigateStage(int direction) void
        -simulateCurrentStage() void
        -drawBracketConnectors(List stageNodes, Pane overlayPane) void
    }

    class TournamentManager {
        -List~Team~ allTeams
        -List~Group~ groups
        -List~Match~ knockoutRound32Matches
        -List~Match~ knockoutRound16Matches
        -List~Match~ quarterFinalMatches
        -List~Match~ semiFinalMatches
        -Match thirdPlaceMatch
        -Match finalMatch
        -int currentKnockoutStageIndex
        +setupGroups(boolean shuffle) void
        +runGroupStage() void
        +determineKnockoutQualifiers() void
        +setupRoundOf32() List~Match~
        +setupRoundOf16() List~Match~
        +setupQuarterFinals() List~Match~
        +setupSemiFinals() List~Match~
        +setupFinals() void
    }

    class Group {
        -String name
        -List~Team~ teams
        -List~Match~ matches
        +addMatch(Match match) void
        +sortGroupTable() void
    }

    class Team {
        -String countryName
        -int fifaRanking
        -int winProbabilityRank
        -int historicalTitles
        -int points
        -int goalsFor
        -int goalsAgainst
        -List~Player~ roster
        +resetTableStats() void
        +getGoalDifference() int
        +updateStats(int gf, int ga, int pointsGained) void
    }

    class Match {
        -Team teamA
        -Team teamB
        -int scoreA
        -int scoreB
        -boolean isPlayed
        -String location
        -String kickoffTime
        +playMatch() void
        +getWinner() Team
    }

    class Player {
        -String name
        -String position
        -int historicalGoals
        -int simGoals
        -int simAssists
        +resetSimStats() void
        +addGoal() void
    }

    Main --> TournamentManager
    Main --> KnockoutStageView
    KnockoutStageView --> TournamentManager
    TournamentManager "1" *-- "12" Group
    TournamentManager "1" *-- "48" Team
    Group "1" o-- "4" Team
    Group "1" *-- "6" Match
    Team "1" *-- "23" Player
    Match "1" o-- "2" Team