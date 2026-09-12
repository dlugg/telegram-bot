package service;

import model.GuessGame;
import model.HumanGuessResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuessService {
    private final Map<Long, GuessGame> games = new HashMap<>();
    private final Random rand = new Random();

    public void startGame(long chatId) {
        games.put(chatId, new GuessGame(rand.nextInt(1, 10 + 1)));

    }

    public GuessGame getGame(long chatId) {
        GuessGame guessGame = games.get(chatId);
        if (guessGame == null) {
            throw new IllegalStateException("игра еще не начата для пользователя: " + chatId);
        } else {
            return guessGame;
        }
    }

    public void endGame(long chatId) {
        games.remove(chatId);
    }

    public HumanGuessResult compare(int secret, int guess) {
        if (secret > guess) {
            return HumanGuessResult.TOO_LOW;
        } else if (secret < guess) {
            return HumanGuessResult.TOO_HIGH;
        } else {
            return HumanGuessResult.EQUAL;
        }
    }

    public HumanGuessResult humanGuessResult(long chatId, int guess) {
        GuessGame guessGame = getGame(chatId);
        int secret = guessGame.getSecretNumber();
        guessGame.increaseAttempts();
        return compare(secret, guess);
    }


}
