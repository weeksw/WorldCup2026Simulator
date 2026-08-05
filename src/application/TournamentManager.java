/**
 * 
 */
package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 */
public class TournamentManager {
	private List<Team> allTeams;
    private List<Group> groups;
    private List<Team> knockoutTeamsStage32;
    
    // 2026 Host Cities matrix for match tagging
    private final String[] hostCities = {
        "New York/New Jersey", "Dallas", "Atlanta", "Los Angeles", "Miami", 
        "San Francisco", "Seattle", "Houston", "Philadelphia", "Kansas City", 
        "Boston", "Toronto", "Vancouver", "Mexico City", "Monterrey", "Guadalajara"
    };

    public TournamentManager() {
        this.allTeams = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.knockoutTeamsStage32 = new ArrayList<>();
        initializeData();
    }

    /**
     * Seeds the initial 48 teams and builds mock player rosters for them.
     */
    private void initializeData() {
        // From 'The Athletic' rankings as of April 1st, 2026
        // Format: Country, FIFA Rank, Win Prob Rank (1-48), Historical Titles
        String[][] teamSeeds = {
        	{"Spain", "2", "1", "1"}, {"Argentina", "3", "2", "3"},{"France", "1", "3", "2"}, {"Brasil", "6", "4", "5"},  
        	{"Netherlands", "7", "5", "0"}, {"England", "4", "6", "0"}, {"Portugal", "5", "7", "0"}, {"Germany", "10", "8", "4"}, 
            {"Colombia", "13", "9", "0"}, {"Croatia", "11", "10", "0"}, {"Morocco", "8", "11", "0"}, {"Uruguay", "17", "12", "2"},
            {"Belgium", "9", "13", "0"}, {"Senegal", "14", "14", "0"}, {"Egypt", "29", "15", "0"}, {"South Korea", "25", "16", "0"},  
            {"Ecuador", "23", "17", "0"},  {"Mexico", "15", "18", "0"}, {"Norway", "31", "19", "0"}, {"Ivory Coast", "34", "20", "0"},
            {"Japan", "18", "21", "0"}, {"Switzerland", "19", "22", "0"}, {"USA", "16", "23", "0"}, {"Turkey", "22", "24", "0"},
            {"Australiia", "27", "25", "0"}, {"Ghana", "74", "26", "0"}, {"Algeria", "28", "27", "0"}, {"Iran", "21", "28", "0"},
            {"Austria", "24", "29", "0"}, {"Canada", "30", "30", "0"}, {"Paraguay", "40", "31", "0"}, {"Saudi Arabia", "61", "32", "0"},
            {"Sweden", "38", "33", "0"}, {"Panama", "33", "34", "0"}, {"Scotland", "43", "35", "0"}, {"Tunisia", "44", "36", "0"},
            {"South Africa", "60", "37", "0"}, {"Qatar", "55", "38", "0"}, {"Czech Republic", "41", "39", "0"}, {"New Zealand", "85", "40", "0"},
            {"Jordan", "63", "41", "0"}, {"Bosnia and Herzegovina", "65", "42", "0"}, {"DR Congo", "46", "43", "0"}, {"Cape Verde", "69", "44", "0"},
            {"Uzbekistan", "50", "45", "0"}, {"Iraq", "57", "46", "0"}, {"Curacao", "82", "47", "0"}, {"Haiti", "83", "48", "0"}
        };

        for (String[] t : teamSeeds) {
            Team team = new Team(t[0], Integer.parseInt(t[1]), Integer.parseInt(t[2]), Integer.parseInt(t[3]));
            
            // Populate a fast mock roster of 23 players per team
            for (int i = 1; i <= 23; i++) {
                String pos = (i <= 3) ? "Goalkeeper" : (i <= 10) ? "Defender" : (i <= 18) ? "Midfielder" : "Forward";
                team.addPlayer(new Player(team.getCountryName() + " Player " + i, pos, 0, 0));
            }
            allTeams.add(team);
        }
    }

    /**
     * Splits the 48 teams into 12 groups (A through L) of 4 teams each.
     */
    public void setupGroups(boolean shuffle) {
        groups.clear();
        if (shuffle) {
            Collections.shuffle(allTeams);
        } else {
            // Sort by win probability rank to balance groups naturally if not shuffling
            allTeams.sort(Comparator.comparingInt(Team::getWinProbabilityRank));
        }

        char groupLetter = 'A';
        for (int i = 0; i < 48; i += 4) {
            Group g = new Group(String.valueOf(groupLetter), allTeams.subList(i, i + 4));
            groups.add(g);
            groupLetter++;
        }
    }

    /**
     * Simulates all group stage fixtures (6 matches per group * 12 groups = 72 matches).
     */
    public void runGroupStage() {
        int cityIndex = 0;
        for (Group g : groups) {
            g.getMatches().clear(); // Clear previous runs if re-simulated
            List<Team> t = g.getTeams();
            int[][] fixtures = {{0,1}, {2,3}, {0,2}, {1,3}, {0,3}, {1,2}};
            
            for (int[] pair : fixtures) {
                String city = hostCities[cityIndex % hostCities.length];
                cityIndex++;
                Match m = new Match(t.get(pair[0]), t.get(pair[1]), city, "18:00 UTC", "Group " + g.getName(), false);
                m.playMatch();
                
                g.addMatch(m); // <-- ADD THIS LINE
            }
            g.sortGroupTable();
        }
        determineKnockoutQualifiers();
    }

    /**
     * Evaluates group tables to advance the top 2 teams from all 12 groups,
     * plus computes the custom tiebreaker ranking to find the top 8 third-place wildcards.
     */
    private void determineKnockoutQualifiers() {
        knockoutTeamsStage32.clear();
        List<Team> allThirdPlaceTeams = new ArrayList<>();

        for (Group g : groups) {
            List<Team> sortedTable = g.getTeams();
            knockoutTeamsStage32.add(sortedTable.get(0)); // 1st Place advances
            knockoutTeamsStage32.add(sortedTable.get(1)); // 2nd Place advances
            allThirdPlaceTeams.add(sortedTable.get(2));   // 3rd Place goes to wildcard pool
        }

        // Custom comparator sorting across all 3rd place teams using World Cup tiebreakers
        allThirdPlaceTeams.sort((t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) return Integer.compare(t2.getPoints(), t1.getPoints());
            if (t1.getGoalDifference() != t2.getGoalDifference()) return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
        });

        // Top 8 third-place finishers advance to round of 32
        for (int i = 0; i < 8; i++) {
            knockoutTeamsStage32.add(allThirdPlaceTeams.get(i));
        }
    }

    public List<Group> getGroups() { return groups; }
    public List<Team> getKnockoutTeamsStage32() { return knockoutTeamsStage32; }
}
