package application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TournamentManager {
    private List<Team> allTeams;
    private List<Group> groups;
    private List<Team> knockoutTeamsStage32;
    private List<Match> knockoutRound16Matches = new ArrayList<>();
    private List<Match> quarterFinalMatches = new ArrayList<>();
    private List<Match> semiFinalMatches = new ArrayList<>();
    private Match thirdPlaceMatch;
    private Match finalMatch;
    private Team champion;
    private int currentKnockoutStageIndex = 0; // 0: RO32, 1: RO16, 2: QF, 3: SF, 4: Finals
    private String userChosenTeamName = "";   // Populated in Focused Mode
    private String mode = "Spectate";         // Added data field (default to "Spectate")
    
    public int getCurrentKnockoutStageIndex() { return currentKnockoutStageIndex; }
    public void setCurrentKnockoutStageIndex(int index) { this.currentKnockoutStageIndex = index; }

    public String getUserChosenTeamName() { return userChosenTeamName; }
    public void setUserChosenTeamName(String name) { this.userChosenTeamName = name; }

    // Mode Getter and Setter
    public String getMode() { return this.mode; }
    public void setMode(String mode) { this.mode = mode; }
    
    /**
     * Checks if the user's chosen team is still active in the tournament or eliminated.
     */
    public boolean isUserTeamEliminated(Match lastMatchPlayed) {
        if (userChosenTeamName == null || userChosenTeamName.isEmpty()) return false;
        
        if (lastMatchPlayed.getTeamA().getCountryName().equalsIgnoreCase(userChosenTeamName) ||
            lastMatchPlayed.getTeamB().getCountryName().equalsIgnoreCase(userChosenTeamName)) {
            return !lastMatchPlayed.getWinner().getCountryName().equalsIgnoreCase(userChosenTeamName);
        }
        return false;
    }
    
    /**
     * Checks if the user's team qualified out of the Group Stage.
     * Returns true if the team was ELIMINATED (did not qualify).
     */
    public boolean isUserTeamEliminatedInGroupStage() {
        if (userChosenTeamName == null || userChosenTeamName.isEmpty()) return false;

        // Get list of all teams that advanced to Round of 32
        List<Match> ro32Matches = getKnockoutRound32Matches();
        if (ro32Matches.isEmpty()) {
            ro32Matches = setupRoundOf32();
        }

        // Check if user's team is in any Round of 32 match
        for (Match m : ro32Matches) {
            if (m.getTeamA().getCountryName().equalsIgnoreCase(userChosenTeamName) ||
                m.getTeamB().getCountryName().equalsIgnoreCase(userChosenTeamName)) {
                return false; // Team made it to RO32, NOT eliminated!
            }
        }

        return true; // Team was eliminated in group stage
    }

    // Checks if a specific team finished top 2 in their group (or top 8 third-place teams)
    public boolean didTeamQualifyForKnockout(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            return false;
        }

        // Search through round of 32 matches to see if the team made the cut
        List<Match> ro32Matches = getKnockoutRound32Matches();
        if (ro32Matches == null || ro32Matches.isEmpty()) {
            ro32Matches = setupRoundOf32();
        }

        for (Match m : ro32Matches) {
            if (m.getTeamA().getCountryName().equalsIgnoreCase(countryName) ||
                m.getTeamB().getCountryName().equalsIgnoreCase(countryName)) {
                return true; // Team successfully reached the knockout stage
            }
        }

        return false; // Team failed to qualify
    }
    
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
            
            for (int i = 1; i <= 23; i++) {
                String pos = (i <= 3) ? "Goalkeeper" : (i <= 10) ? "Defender" : (i <= 18) ? "Midfielder" : "Forward";
                team.addPlayer(new Player(team.getCountryName() + " Player " + i, pos, 0, 0));
            }
            allTeams.add(team);
        }
    }

    public void setupGroups(boolean shuffle) {
        groups.clear();
        if (shuffle) {
            Collections.shuffle(allTeams);
        } else {
            allTeams.sort(Comparator.comparingInt(Team::getWinProbabilityRank));
        }

        char groupLetter = 'A';
        for (int i = 0; i < 48; i += 4) {
            Group g = new Group(String.valueOf(groupLetter), allTeams.subList(i, i + 4));
            groups.add(g);
            groupLetter++;
        }
    }

    public void runGroupStage() {
        int cityIndex = 0;
        for (Group g : groups) {
            g.getMatches().clear();
            List<Team> t = g.getTeams();
            int[][] fixtures = {{0,1}, {2,3}, {0,2}, {1,3}, {0,3}, {1,2}};
            
            for (int[] pair : fixtures) {
                String city = hostCities[cityIndex % hostCities.length];
                cityIndex++;
                Match m = new Match(t.get(pair[0]), t.get(pair[1]), city, "18:00 UTC", "Group " + g.getName(), false);
                m.playMatch();
                g.addMatch(m);
            }
            g.sortGroupTable();
        }
        determineKnockoutQualifiers();
    }

    private void determineKnockoutQualifiers() {
        knockoutTeamsStage32.clear();
        List<Team> allThirdPlaceTeams = new ArrayList<>();

        for (Group g : groups) {
            List<Team> sortedTable = g.getTeams();
            knockoutTeamsStage32.add(sortedTable.get(0));
            knockoutTeamsStage32.add(sortedTable.get(1));
            allThirdPlaceTeams.add(sortedTable.get(2));
        }

        allThirdPlaceTeams.sort((t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) return Integer.compare(t2.getPoints(), t1.getPoints());
            if (t1.getGoalDifference() != t2.getGoalDifference()) return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
        });

        for (int i = 0; i < 8; i++) {
            knockoutTeamsStage32.add(allThirdPlaceTeams.get(i));
        }
    }

    public List<Group> getGroups() { return groups; }
    public List<Team> getKnockoutTeamsStage32() { return knockoutTeamsStage32; }
    
    private List<Match> knockoutRound32Matches = new ArrayList<>();

    public List<Match> setupRoundOf32() {
        knockoutRound32Matches.clear();

        List<Team> winners = new ArrayList<>();
        List<Team> runnersUp = new ArrayList<>();
        List<Team> thirdPlacePool = new ArrayList<>();

        for (Group g : groups) {
            List<Team> table = g.getTeams();
            winners.add(table.get(0));
            runnersUp.add(table.get(1));
            thirdPlacePool.add(table.get(2));
        }

        thirdPlacePool.sort((t1, t2) -> {
            if (t1.getPoints() != t2.getPoints()) return Integer.compare(t2.getPoints(), t1.getPoints());
            if (t1.getGoalDifference() != t2.getGoalDifference()) return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());
            return Integer.compare(t2.getGoalsFor(), t1.getGoalsFor());
        });

        List<Team> top8Thirds = new ArrayList<>(thirdPlacePool.subList(0, 8));

        top8Thirds.sort((t1, t2) -> Integer.compare(t1.getWinProbabilityRank(), t2.getWinProbabilityRank()));
        winners.sort((t1, t2) -> Integer.compare(t1.getWinProbabilityRank(), t2.getWinProbabilityRank()));

        int cityIndex = 0;

        for (int i = 0; i < 8; i++) {
            Team winner = winners.get(i);
            Team third = top8Thirds.get(7 - i);
            String city = hostCities[cityIndex % hostCities.length];
            cityIndex++;

            knockoutRound32Matches.add(new Match(winner, third, city, "17:00 UTC", "Round of 32", true));
        }

        for (int i = 0; i < 4; i++) {
            Team winner = winners.get(8 + i);
            Team runnerUp = runnersUp.get(i);
            String city = hostCities[cityIndex % hostCities.length];
            cityIndex++;

            knockoutRound32Matches.add(new Match(winner, runnerUp, city, "20:00 UTC", "Round of 32", true));
        }

        for (int i = 4; i < 12; i += 2) {
            Team r1 = runnersUp.get(i);
            Team r2 = runnersUp.get(i + 1);
            String city = hostCities[cityIndex % hostCities.length];
            cityIndex++;

            knockoutRound32Matches.add(new Match(r1, r2, city, "14:00 UTC", "Round of 32", true));
        }

        return knockoutRound32Matches;
    }

    public List<Match> getKnockoutRound32Matches() { return knockoutRound32Matches; }
    
    public List<Match> setupRoundOf16() {
        knockoutRound16Matches.clear();
        int cityIndex = 0;

        for (int i = 0; i < knockoutRound32Matches.size(); i += 2) {
            Team winner1 = knockoutRound32Matches.get(i).getWinner();
            Team winner2 = knockoutRound32Matches.get(i + 1).getWinner();

            String city = hostCities[cityIndex % hostCities.length];
            cityIndex++;

            knockoutRound16Matches.add(new Match(winner1, winner2, city, "18:00 UTC", "Round of 16", true));
        }
        return knockoutRound16Matches;
    }

    public List<Match> setupQuarterFinals() {
        quarterFinalMatches.clear();
        int cityIndex = 4;

        for (int i = 0; i < knockoutRound16Matches.size(); i += 2) {
            Team winner1 = knockoutRound16Matches.get(i).getWinner();
            Team winner2 = knockoutRound16Matches.get(i + 1).getWinner();

            String city = hostCities[cityIndex % hostCities.length];
            cityIndex++;

            quarterFinalMatches.add(new Match(winner1, winner2, city, "19:00 UTC", "Quarter-Final", true));
        }
        return quarterFinalMatches;
    }

    public List<Match> setupSemiFinals() {
        semiFinalMatches.clear();

        Team w1 = quarterFinalMatches.get(0).getWinner();
        Team w2 = quarterFinalMatches.get(1).getWinner();
        Team w3 = quarterFinalMatches.get(2).getWinner();
        Team w4 = quarterFinalMatches.get(3).getWinner();

        semiFinalMatches.add(new Match(w1, w2, "Atlanta", "20:00 UTC", "Semi-Final", true));
        semiFinalMatches.add(new Match(w3, w4, "Dallas", "20:00 UTC", "Semi-Final", true));

        return semiFinalMatches;
    }

    public void setupFinals() {
        Match sf1 = semiFinalMatches.get(0);
        Match sf2 = semiFinalMatches.get(1);

        Team sf1Winner = sf1.getWinner();
        Team sf1Loser = (sf1.getWinner() == sf1.getTeamA()) ? sf1.getTeamB() : sf1.getTeamA();

        Team sf2Winner = sf2.getWinner();
        Team sf2Loser = (sf2.getWinner() == sf2.getTeamA()) ? sf2.getTeamB() : sf2.getTeamA();

        thirdPlaceMatch = new Match(sf1Loser, sf2Loser, "Miami", "17:00 UTC", "3rd Place Match", true);
        finalMatch = new Match(sf1Winner, sf2Winner, "New York/New Jersey", "20:00 UTC", "World Cup Final", true);
    }

    public List<Match> getKnockoutRound16Matches() { return knockoutRound16Matches; }
    public List<Match> getQuarterFinalMatches() { return quarterFinalMatches; }
    public List<Match> getSemiFinalMatches() { return semiFinalMatches; }
    public Match getThirdPlaceMatch() { return thirdPlaceMatch; }
    public Match getFinalMatch() { return finalMatch; }

    public Team getChampion() { return champion; }
    public void setChampion(Team champion) { this.champion = champion; }
}