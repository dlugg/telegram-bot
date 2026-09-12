package repository;

import model.RpsGameResult;
import model.RpsRoundResult;
import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RpsRoundsRepository {
    private final Database database;
    private final UserRepository userRepository;

    public RpsRoundsRepository(Database database, UserRepository userRepository) {
        this.database = database;
        this.userRepository = userRepository;
    }

    public void addRound(long chatId, RpsGameResult result) throws SQLException {

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId = userRepository.findOrCreateUser(connection, chatId);
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO rps_rounds(user_id, result)  VALUES (?,?)")) {
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setString(2, result.name());
                    int inserted = preparedStatement.executeUpdate();
                    if (inserted < 1) {
                        throw new SQLException("INSERT game result into rps_rounds affected 0 rows for chat_id " + chatId);
                    }
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public Map<RpsGameResult, Integer> getStats(long chatId) throws SQLException {
        Map<RpsGameResult, Integer> usersRpsStats = new HashMap<>();
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     """
                             SELECT result, COUNT(*) AS count
                             FROM users JOIN rps_rounds on users.id = rps_rounds.user_id
                             WHERE chat_id = ?
                             GROUP BY result
                             """)) {
            preparedStatement.setLong(1, chatId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    usersRpsStats.put(RpsGameResult.valueOf(resultSet.getString("result")), resultSet.getInt("count"));
                }
            }
            for (RpsGameResult rpsGameResultValue : RpsGameResult.values()) {
                usersRpsStats.putIfAbsent(rpsGameResultValue, 0);
            }
        }
        return usersRpsStats;
    }
}
