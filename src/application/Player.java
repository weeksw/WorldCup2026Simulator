package application;

/**
 * Model class representing an individual athlete on a national squad roster.
 * <p>
 * Contains field position metadata, pre-2026 career stats, and live tracking hooks 
 * for simulated match events (goals, assists, saves, and cards).
 * </p>
 * <p>
 * <b>Future Expansion Note:</b> While squad rosters are fully populated during tournament 
 * initialization, individual player-level match events are not yet actively simulated by 
 * the core match engine. This class serves as a ready-to-use foundation for future features, 
 * such as a Golden Boot (Top Scorer) Leaderboard, Golden Glove (Best Goalkeeper) Tracker, 
 * or a Yellow/Red Card Suspension Engine.
 * </p>
 * 
 * @author William Weeks
 * @version 1.0
 */

public class Player {

    private String name;
    private String position; // "Forward", "Midfielder", "Defender", "Goalkeeper"
    
    // Historical baseline career statistics (Prior to current tournament)
    private int historicalGoals;
    private int historicalAssists;
    
    // Live simulator trackers (Reset per simulation run)
    private int simGoals;
    private int simAssists;
    private int simSaves;
    private int simYellowCards;
    private int simRedCards;

    /**
     * Constructs a new Player instance with position details and career baselines.
     * 
     * @param name Full name of the athlete.
     * @param position Field position ("Forward", "Midfielder", "Defender", "Goalkeeper").
     * @param historicalGoals Total international goals prior to simulation.
     * @param historicalAssists Total international assists prior to simulation.
     */
    
    public Player(String name, String position, int historicalGoals, int historicalAssists) {
    	
        this.name = name;
        this.position = position;
        this.historicalGoals = historicalGoals;
        this.historicalAssists = historicalAssists;
        resetSimStats();
    }

    /**
     * Clears all tournament simulation statistics back to zero.
     */
    
    public void resetSimStats() {
    	
        this.simGoals = 0;
        this.simAssists = 0;
        this.simSaves = 0;
        this.simYellowCards = 0;
        this.simRedCards = 0;
    }

    // Helper incrementors designed for future Match Engine integration
    public void addGoal() { this.simGoals++; }
    public void addAssist() { this.simAssists++; }
    public void addSave() { this.simSaves++; }
    public void addYellowCard() { this.simYellowCards++; }
    public void addRedCard() { this.simRedCards++; }

    // Getters
    public String getName() { return name; }
    public String getPosition() { return position; }
    public int getHistoricalGoals() { return historicalGoals; }
    public int getHistoricalAssists() { return historicalAssists; }
    public int getSimGoals() { return simGoals; }
    public int getSimAssists() { return simAssists; }
    public int getSimSaves() { return simSaves; }
    public int getSimYellowCards() { return simYellowCards; }
    public int getSimRedCards() { return simRedCards; }
}