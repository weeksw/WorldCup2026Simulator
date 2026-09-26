package application;

import java.util.ArrayList;
import java.util.List;

/**
 * Core model representing a national football team participating in the tournament.
 * <p>
 * Stores static seed data, historical background statistics, active roster links, 
 * and live group-stage table statistics calculated during tournament simulation.
 * </p>
 * <p>
 * <b>Future Expansion Note:</b> Properties such as {@code historicalTitles} and the full 
 * {@code roster} list are instantiated during setup and are ready for future UI additions,
 * such as a Team Profile Inspector, Squad Roster View, or Historical Trophy Counter.
 * </p>
 * 
 * @author William Weeks
 * @version 1.0
 */

public class Team {
	
    private String countryName;
    private int fifaRanking;
    private int winProbabilityRank; // 1 (Strongest, e.g., Argentina/France) to 48 (Underdog)
    private int historicalTitles;
    private List<Player> roster;

    // Live Group Stage Standings Statistics
    private int matchesPlayed;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    /**
     * Constructs a new Team object with static seeding metadata and resets table statistics.
     * 
     * @param countryName Name of the nation.
     * @param fifaRanking World FIFA ranking integer.
     * @param winProbabilityRank Relative strength ranking index (1–48).
     * @param historicalTitles Count of previous World Cup trophies won.
     */
    
    public Team(String countryName, int fifaRanking, int winProbabilityRank, int historicalTitles) {
    	
        this.countryName = countryName;
        this.fifaRanking = fifaRanking;
        this.winProbabilityRank = winProbabilityRank;
        this.historicalTitles = historicalTitles;
        this.roster = new ArrayList<>();
        resetTableStats();
    }

    /**
     * Resets all live group stage performance counters to zero for fresh simulation runs.
     */
    
    public void resetTableStats() {
    	
        this.matchesPlayed = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.points = 0;
    }

    /**
     * Adds an individual player to the team's 23-man squad roster.
     * 
     * @param player The Player instance to add.
     */
    
    public void addPlayer(Player player) {
    	
        this.roster.add(player);
    }

    /**
     * Calculates the net goal differential (Goals For minus Goals Against).
     * 
     * @return int Net goal differential.
     */
    
    public int getGoalDifference() {
    	
        return this.goalsFor - this.goalsAgainst;
    }

    /**
     * Updates cumulative group table statistics following the conclusion of a match.
     * 
     * @param gf Goals scored by this team.
     * @param ga Goals conceded by this team.
     * @param pointsGained Standings points awarded (3 for Win, 1 for Draw, 0 for Loss).
     */
    
    public void updateStats(int gf, int ga, int pointsGained) {
    	
        this.matchesPlayed++;
        this.goalsFor += gf;
        this.goalsAgainst += ga;
        this.points += pointsGained;
        if (pointsGained == 3) this.wins++;
        else if (pointsGained == 1) this.draws++;
        else this.losses++;
    }

    // Getters
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
}