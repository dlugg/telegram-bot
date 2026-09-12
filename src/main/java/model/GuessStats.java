package model;

public class GuessStats {
    private final int gamesPlayed;
    private final Integer minAttempts;

    public GuessStats(int gamesPlayed, Integer minAttempts) {
        this.gamesPlayed = gamesPlayed;
        this.minAttempts = minAttempts;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public Integer getMinAttempts() {
        return minAttempts;
    }
}
