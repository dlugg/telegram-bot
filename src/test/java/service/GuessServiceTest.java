package service;

import model.GuessStats;
import model.HumanGuessResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.GuessGamesRepository;
import repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GuessServiceTest {

    Database database = new Database("jdbc:postgresql://localhost:5432/javabot_test", "postgres", System.getenv("DATABASE_PASSWORD"));
    UserRepository userRepository = new UserRepository(database);
    GuessGamesRepository guessGamesRepository = new GuessGamesRepository(database, userRepository);
    GuessService guessService = new GuessService(guessGamesRepository, new Random(67));

    @BeforeEach
    void clearGuessGamesAndUsersTables() throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("TRUNCATE tasks, users, rps_rounds, guess_games RESTART IDENTITY")) {
            statement.execute();
        }
    }

    @Test
    void guessBelowSecretIsTooLow() {
        HumanGuessResult humanGuessResult = guessService.compare(2, 1);
        assertEquals(HumanGuessResult.TOO_LOW, humanGuessResult);
    }

    @Test
    void guessAboveSecretIsTooHigh() {
        HumanGuessResult humanGuessResult = guessService.compare(2, 3);
        assertEquals(HumanGuessResult.TOO_HIGH, humanGuessResult);
    }

    @Test
    void matchingGuessIsEqual() {
        HumanGuessResult humanGuessResult = guessService.compare(2, 2);
        assertEquals(HumanGuessResult.EQUAL, humanGuessResult);
    }

    @Test
    void notStartedGameThrowsException() {
        assertThrows(IllegalStateException.class, () -> guessService.getGame(0));
    }

    @Test
    void wonGameIsSavedWithAttemptsCount() throws SQLException {
        long chatId = 123;
        int secret = new Random(67).nextInt(1, 11);
        guessService.startGame(chatId);

        guessService.humanGuessResult(chatId, secret + 1);
        guessService.humanGuessResult(chatId, secret - 1);
        guessService.humanGuessResult(chatId, secret);
        GuessStats guessStats = guessGamesRepository.getStats(chatId);
        assertEquals(3, guessStats.getMinAttempts());
        assertEquals(1, guessStats.getGamesPlayed());

    }

    @Test
    void wonGameCannotBeContinued(){
        long chatId = 123;
        int secret = new Random(67).nextInt(1, 11);
        guessService.startGame(chatId);

        guessService.humanGuessResult(chatId, secret + 1);
        guessService.humanGuessResult(chatId, secret);
        assertThrows(IllegalStateException.class, () -> guessService.getGame(chatId));
    }
}
