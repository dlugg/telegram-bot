package repository;

import model.GuessStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class GuessGamesRepositoryTest {
    Database database = new Database("jdbc:postgresql://localhost:5432/javabot_test", "postgres", System.getenv("DATABASE_PASSWORD"));
    UserRepository userRepository = new UserRepository(database);
    GuessGamesRepository guessGamesRepository = new GuessGamesRepository(database, userRepository);

    @BeforeEach
    void clearGuessGamesAndUsersTables() throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("TRUNCATE tasks, users, rps_rounds, guess_games RESTART IDENTITY")) {
            statement.execute();
        }
    }

    @Test
    void addedGameAppearsAtStats() throws SQLException {
        long chatId = 123;
        guessGamesRepository.addGame(chatId, 4);
        GuessStats guessStats = guessGamesRepository.getStats(chatId);
        GuessStats expectedGuessStats = new GuessStats(1, 4);
        assertEquals(expectedGuessStats.getGamesPlayed(), guessStats.getGamesPlayed());
        assertEquals(expectedGuessStats.getMinAttempts(), guessStats.getMinAttempts());
    }

    @Test
    void manyGamesGiveCountAndLowestAttempts() throws SQLException {
        long chatId = 123;
        guessGamesRepository.addGame(chatId, 4);
        guessGamesRepository.addGame(chatId, 3);
        guessGamesRepository.addGame(chatId, 6);
        guessGamesRepository.addGame(chatId, 7);
        GuessStats guessStats = guessGamesRepository.getStats(chatId);
        GuessStats expectedGuessStats = new GuessStats(4, 3);
        assertEquals(expectedGuessStats.getGamesPlayed(), guessStats.getGamesPlayed());
        assertEquals(expectedGuessStats.getMinAttempts(), guessStats.getMinAttempts());
    }

    @Test
    void usersSeesOnlyHisStats() throws SQLException {
        long chatIdFirstUser = 123;
        long chatIdSecondUser = 456;
        guessGamesRepository.addGame(chatIdFirstUser, 4);
        guessGamesRepository.addGame(chatIdSecondUser, 3);
        GuessStats guessStatsFirstUser = guessGamesRepository.getStats(chatIdFirstUser);
        GuessStats guessStatsSecondUser = guessGamesRepository.getStats(chatIdSecondUser);
        assertEquals(1,guessStatsFirstUser.getGamesPlayed());
        assertEquals(4,guessStatsFirstUser.getMinAttempts());

        assertEquals(1,guessStatsSecondUser.getGamesPlayed());
        assertEquals(3,guessStatsSecondUser.getMinAttempts());
    }

    @Test
    void nonExistingChatIdReturnsZeroesAndNullsAtStats() throws SQLException {
        GuessStats guessStats = guessGamesRepository.getStats(123);
        assertEquals(0, guessStats.getGamesPlayed());
        assertNull(guessStats.getMinAttempts());
    }
}
