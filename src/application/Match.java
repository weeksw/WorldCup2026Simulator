/**
 * 
 */
package application;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 
 */
public class Match {
	
	private Team teamA;
    private Team teamB;
    private int scoreA;
    private int scoreB;
    private String location;
    private String kickoffTime;
    private String stage; // e.g., "Group Stage", "Round of 32", "Final"
    private boolean isKnockout;
    private Random random;

    public Match(Team teamA, Team teamB, String location, String kickoffTime, String stage, boolean isKnockout) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.location = location;
        this.kickoffTime = kickoffTime;
        this.stage = stage;
        this.isKnockout = isKnockout;
        this.random = new Random();
    }

    /**
     * Executes the match logic, determines scores, updates group standings, 
     * and distributes performance statistics to individual players.
     */
    public void playMatch() {
        // 1. Calculate base performance weights (Lower rank number = higher skill)
        // A rank of 1 gives a weight of 48. A rank of 48 gives a weight of 1.
        double weightA = 49 - teamA.getWinProbabilityRank();
        double weightB = 49 - teamB.getWinProbabilityRank();
        
        // Calculate relative advantage
        double totalWeight = weightA + weightB;
        double ratioA = weightA / totalWeight;

        // 2. Generate goals using a Poisson-style distribution baseline
        // Average total match goals in World Cups hover around 2.5 to 3.0
        int totalExpectedGoals = random.nextInt(4); // 0, 1, 2, or 3 base goals
        int extraGoals = (random.nextDouble() < 0.3) ? random.nextInt(3) : 0; // 30% chance for higher-scoring games
        int totalGoals = totalExpectedGoals + extraGoals;

        this.scoreA = 0;
        this.scoreB = 0;

        // Allocate goals based on team ratios
        for (int i = 0; i < totalGoals; i++) {
            if (random.nextDouble() < ratioA) {
                scoreA++;
            } else {
                scoreB++;
            }
        }

        // 3. Handle Extra Time / Penalty Shootouts for Knockout Stages
        if (isKnockout && scoreA == scoreB) {
            // Simulate Extra Time goals (Weighted)
            if (random.nextDouble() < 0.4) { // 40% chance someone scores in extra time
                if (random.nextDouble() < ratioA) scoreA++;
                else scoreB++;
            }
            
            // If still tied, simulate a sudden death penalty shootout variable
            if (scoreA == scoreB) {
                if (random.nextDouble() < ratioA) {
                    scoreA++; // Simple representation: Favorite wins the shootout
                } else {
                    scoreB++;
                }
            }
        }

        // 4. Distribute Match Events to Players
        distributePlayerStats(teamA, scoreA, scoreB);
        distributePlayerStats(teamB, scoreB, scoreA);

        // 5. Update Team Standings (Only relevant during the Group Stage)
        if (!isKnockout) {
            if (scoreA > scoreB) {
                teamA.updateStats(scoreA, scoreB, 3);
                teamB.updateStats(scoreB, scoreA, 0);
            } else if (scoreB > scoreA) {
                teamA.updateStats(scoreA, scoreB, 0);
                teamB.updateStats(scoreB, scoreA, 3);
            } else {
                teamA.updateStats(scoreA, scoreB, 1);
                teamB.updateStats(scoreB, scoreA, 1);
            }
        }
    }

    /**
     * Loops through individual goals scored and assigns them alongside assists, 
     * saves, and disciplinary cards to random roster positions.
     */
    private void distributePlayerStats(Team team, int goalsScored, int goalsConceded) {
        List<Player> roster = team.getRoster();
        if (roster.isEmpty()) return;

        // Distribute Goals and Assists
        for (int i = 0; i < goalsScored; i++) {
            // Forwards/Midfielders are more likely to score (pick from first 15 players typically)
            Player scorer = roster.get(random.nextInt(Math.min(15, roster.size())));
            scorer.addGoal();

            // 70% chance of an assist on a goal
            if (random.nextDouble() < 0.7) {
                Player assistant = roster.get(random.nextInt(roster.size()));
                if (assistant != scorer) {
                    assistant.addAssist();
                }
            }
        }

        // Distribute Saves to Goalkeepers (Usually last players in a roster array)
        for (Player p : roster) {
            if (p.getPosition().equalsIgnoreCase("Goalkeeper")) {
                // Simulating saves based on shots faced
                int saves = random.nextInt(Math.max(1, 6 - goalsConceded));
                for (int s = 0; s < saves; s++) p.addSave();
            }
        }

        // Distribute Cards (Discipline factors)
        for (Player p : roster) {
            if (random.nextDouble() < 0.15) { // 15% chance a player gets a booking card
                p.addYellowCard();
                if (random.nextDouble() < 0.05) { // 5% chance it turns into a red
                    p.addRedCard();
                }
            }
        }
    }

    // Getters
    public Team getTeamA() { return teamA; }
    public Team getTeamB() { return teamB; }
    public int getScoreA() { return scoreA; }
    public int getScoreB() { return scoreB; }
    public String getLocation() { return location; }
    public String getKickoffTime() { return kickoffTime; }
    public String getStage() { return stage; }
}
