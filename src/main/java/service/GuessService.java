package service;

import exception.DataAccessException;
import model.GuessGame;
import model.GuessStats;
import model.HumanGuessResult;
import repository.GuessGamesRepository;
import repository.UserRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuessService {
    private final Map<Long, GuessGame> games = new HashMap<>();
    private final Random rand;
    private final GuessGamesRepository guessGamesRepository;

    public GuessService(GuessGamesRepository guessGamesRepository) {
        this(guessGamesRepository, new Random());
    }

    public GuessService(GuessGamesRepository guessGamesRepository, Random rand) {
        this.guessGamesRepository = guessGamesRepository;
        this.rand = rand;
    }

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

    private void endGame(long chatId) {
        games.remove(chatId);
    }

    public GuessStats getStats(long chatId) {
        try {
            return guessGamesRepository.getStats(chatId);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось получить статистику для пользователя: " + chatId, e);
        }
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
        HumanGuessResult humanGuessResult = compare(secret, guess);
        if (humanGuessResult == HumanGuessResult.EQUAL) {
            try {
                guessGamesRepository.addGame(chatId, guessGame.getAttempts());
                endGame(chatId);
            } catch (SQLException e) {
                throw new DataAccessException("не удалось добавить результат игры для пользователя: " + chatId, e);
            }
        }

        return humanGuessResult;
    }


}
