package repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {
    Database database = new Database("jdbc:postgresql://localhost:5432/javabot_test", "postgres", System.getenv("DATABASE_PASSWORD"));
    UserRepository userRepository = new UserRepository(database);

    @BeforeEach
    void clearTasksAndUsersTables() throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("TRUNCATE tasks, users, rps_rounds, guess_games RESTART IDENTITY")) {
            statement.execute();
        }
    }

    @Test
    void savedUserNameEqualsNameFromGetUsreName() throws SQLException {
        long chatId = 123;
        userRepository.saveUserName(chatId, "Andrzej");

        assertEquals("Andrzej", userRepository.getUserName(chatId));
    }

    @Test
    void forNonExistingChatIdFindUserMethodReturnsEmptyOptional() throws SQLException {
        long chatId = 123123;
        assertTrue(userRepository.findUserId(chatId).isEmpty());
    }

    @Test
    void deletingUserNameKeepsUserId() throws SQLException {

        long chatId = 123;

        userRepository.saveUserName(chatId, "Andrzej");
        Optional<Long> userId = userRepository.findUserId(chatId);


        userRepository.deleteUserName(chatId);
        assertEquals(userId, userRepository.findUserId(chatId));
    }

    @Test
    void reWritingUserNameDoNotCreateNewUser() throws SQLException {
        long chatId = 123;

        userRepository.saveUserName(chatId, "David");
        Optional<Long> userId = userRepository.findUserId(chatId);

        userRepository.saveUserName(chatId, "NotDavid");

        assertEquals(userId, userRepository.findUserId(chatId));
    }

    @Test
    void returnsSameUserIdWhenCalledTwice() throws SQLException {
        long chatId = 123;

        Long userIdFirstCall = userRepository.findOrCreateUser(chatId);
        Long userIdSecondCall = userRepository.findOrCreateUser(chatId);

        assertEquals(userIdFirstCall, userIdSecondCall);
    }
}