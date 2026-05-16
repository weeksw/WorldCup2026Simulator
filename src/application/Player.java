/**
 * 
 */
package application;

/**
 * 
 */
public class Player {

	private String name;
    private String position; // "Forward", "Midfielder", "Defender", "Goalkeeper"
    
    // All-time historical World Cup stats (Prior to 2026)
    private int historicalGoals;
    private int historicalAssists;
    
    // Live simulator trackers (Resets every new simulation run)
    private int simGoals;
    private int simAssists;
    private int simSaves;
    private int simYellowCards;
    private int simRedCards;

    public Player(String name, String position, int historicalGoals, int historicalAssists) {
        this.name = name;
        this.position = position;
        this.historicalGoals = historicalGoals;
        this.historicalAssists = historicalAssists;
        resetSimStats();
    }

    public void resetSimStats() {
        this.simGoals = 0;
        this.simAssists = 0;
        this.simSaves = 0;
        this.simYellowCards = 0;
        this.simRedCards = 0;
    }

    // Incrementor helper methods for the match engine
    public void addGoal() { this.simGoals++; }
    public void addAssist() { this.simAssists++; }
    public void addSave() { this.simSaves++; }
    public void addYellowCard() { this.simYellowCards++; }
    public void addRedCard() { this.simRedCards++; }

    // Getters and Setters
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
