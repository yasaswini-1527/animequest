package firstphase;

import java.io.Serializable;

/**
 * LeaderboardUser - JavaBean representing a ranked warrior on the Global Leaderboard.
 */
public class LeaderboardUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private int totalXp;
    private int playerLevel;
    private int completedMissions;
    private int rank;
    private String rankTitle;

    public LeaderboardUser() {
    }

    public LeaderboardUser(int id, String username, int totalXp, int playerLevel, int completedMissions, int rank, String rankTitle) {
        this.id = id;
        this.username = username;
        this.totalXp = totalXp;
        this.playerLevel = playerLevel;
        this.completedMissions = completedMissions;
        this.rank = rank;
        this.rankTitle = rankTitle;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public int getCompletedMissions() {
        return completedMissions;
    }

    public void setCompletedMissions(int completedMissions) {
        this.completedMissions = completedMissions;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getRankTitle() {
        return rankTitle;
    }

    public void setRankTitle(String rankTitle) {
        this.rankTitle = rankTitle;
    }
}
