package repository;

import model.GuessStats;
import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuessGamesRepository {
    private final Database database;
    private final UserRepository userRepository;

    public GuessGamesRepository(Database database, UserRepository userRepository) {
        this.database = database;
        this.userRepository = userRepository;
    }

    public void addGame(long chatId, int attempts) throws SQLException {

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId = userRepository.findOrCreateUser(connection, chatId);
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO guess_games(user_id, attempts)  VALUES (?,?)")) {
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setInt(2, attempts);
                    int inserted = preparedStatement.executeUpdate();
                    if (inserted < 1) {
                        throw new SQLException("INSERT guess game result into guess_games affected 0 rows for chat_id " + chatId);
                    }
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public GuessStats getStats(long chatId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     """
                             SELECT COUNT(*) AS count, MIN(attempts) AS min_attempts
                             FROM users JOIN guess_games on users.id = guess_games.user_id
                             WHERE chat_id = ?
                             """)) {
            preparedStatement.setLong(1, chatId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                Integer best = resultSet.getObject("min_attempts", Integer.class);
                int gamesPlayed = resultSet.getInt("count");
                return new GuessStats(gamesPlayed, best);
            }

        }
    }
}
