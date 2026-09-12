package model;

public class GuessGame {
    private final int secretNumber;
    private int attempts;
    public GuessGame(int secretNumber){
        this.secretNumber = secretNumber;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getSecretNumber() {
        return secretNumber;
    }

    public void increaseAttempts(){
        attempts += 1;
    }
}
