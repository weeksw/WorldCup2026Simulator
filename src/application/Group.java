package application;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single four-team group stage pod (e.g., Group A, Group B) in the tournament.
 * <p>
 * Manages the list of assigned national teams, tracks played group fixtures, and handles
 * multi-tier tie-breaking algorithms to maintain accurate group standings.
 * </p>
 * 
 * @author William Weeks
 * @version 1.0
 */

public class Group {
	
    private String name;
    private List<Team> teams;
    private List<Match> matches;

    /**
     * Constructs a new Group instance with a designated letter identifier and team set.
     * 
     * @param name Group designation (e.g., "A", "B").
     * @param assignedTeams List of 4 teams allocated to this group.
     */
    
    public Group(String name, List<Team> assignedTeams) {
        this.name = name;
        this.teams = new ArrayList<>(assignedTeams);
        this.matches = new ArrayList<>();
    }

    /**
     * Appends a completed or scheduled match fixture to the group record.
     * 
     * @param match The Match object to record.
     */
    
    public void addMatch(Match match) {
    	
        this.matches.add(match);
    }

    /**
     * Retrieves all fixtures assigned to this group.
     * 
     * @return List&lt;Match&gt; Group match schedule and results.
     */
    
    public List<Match> getMatches() {
    	
        return matches;
    }

    /**
     * Sorts the group standings table using official FIFA tie-breaking criteria:
     * <ol>
     *   <li>Total Points (Descending)</li>
     *   <li>Goal Difference (Descending)</li>
     *   <li>Goals Scored / Goals For (Descending)</li>
     * </ol>
     */
    
    public void sortGroupTable() {
    	
        this.teams.sort((t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) {
                return Integer.compare(t2.getPoints(), t1.getPoints());
            }
            if (t1.getGoalDifference() != t2.getGoalDifference()) {
                return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            }
            return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
        });
    }

    /**
     * Gets the group identifier name.
     * 
     * @return String Group name (e.g., "A").
     */
    
    public String getName() { 
    	
        return name; 
    }

    /**
     * Gets the current list of teams in this group.
     * 
     * @return List&lt;Team&gt; Participating group teams.
     */
    
    public List<Team> getTeams() { 
    	
        return teams; 
    }
}