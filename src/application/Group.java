/**
 * 
 */
package application;

import java.util.*;

/**
 * 
 */
public class Group {
	private String name; // "A", "B", etc.
    private List<Team> teams;

    public Group(String name, List<Team> assignedTeams) {
        this.name = name;
        this.teams = new ArrayList<>(assignedTeams);
    }

    public void sortGroupTable() {
        this.teams.sort((t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) {
                return Integer.compare(t2.getPoints(), t1.getPoints()); // Descending points
            }
            if (t1.getGoalDifference() != t2.getGoalDifference()) {
                return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference()); // Descending GD
            }
            return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor()); // Descending Goals Scored
        });
    }

    public String getName() { return name; }
    public List<Team> getTeams() { return teams; }
}
