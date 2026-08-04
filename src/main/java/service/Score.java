package service;

public class Score {
    private int wins = 0;
    private int losses = 0;

    public void addWin() {
        wins += 1;
    }

    public void addLoss(){
        losses += 1;
    }

    public int getLosses() {
        return losses;
    }

    public int getWins() {
        return wins;
    }
}
