/**
 * 
 */
package application;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 */

public class Team {
    private String countryName;
    private int fifaRanking;
    private int winProbabilityRank; // 1 (Strongest, e.g., Argentina/France) to 48 (Underdog)
    private int historicalTitles;
    private List<Player> roster;

    // Group Stage Table Statistics
    private int matchesPlayed;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    public Team(String countryName, int fifaRanking, int winProbabilityRank, int historicalTitles) {
        this.countryName = countryName;
        this.fifaRanking = fifaRanking;
        this.winProbabilityRank = winProbabilityRank;
        this.historicalTitles = historicalTitles;
        this.roster = new ArrayList<>();
        resetTableStats();
    }

    public void resetTableStats() {
        this.matchesPlayed = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.points = 0;
    }

    public void addPlayer(Player player) {
        this.roster.add(player);
    }

    // Calculated property for Group Table sorting
    public int getGoalDifference() {
        return this.goalsFor - this.goalsAgainst;
    }

    // Getters and Setters
    public String getCountryName() { return countryName; }
    public int getFifaRanking() { return fifaRanking; }
    public int getWinProbabilityRank() { return winProbabilityRank; }
    public int getHistoricalTitles() { return historicalTitles; }
    public List<Player> getRoster() { return roster; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getPoints() { return points; }

    // Table updating setters called by Match Engine
    public void updateStats(int gf, int ga, int pointsGained) {
        this.matchesPlayed++;
        this.goalsFor += gf;
        this.goalsAgainst += ga;
        this.points += pointsGained;
        if (pointsGained == 3) this.wins++;
        else if (pointsGained == 1) this.draws++;
        else this.losses++;
    }
}