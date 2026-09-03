package repository;

import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    public final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public Long findUserId(long chatId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT id FROM users WHERE chat_id = ?")) {
            preparedStatement.setLong(1, chatId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                } else {
                    return null;
                }
            }


        }

    }

    public long findOrCreateUser(long chatId) throws SQLException {
        Long existing = findUserId(chatId);
        if (existing != null) {
            return existing;
        } else {
            try (Connection connection = database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO users (chat_id) VALUES (?) RETURNING id ")) {
                preparedStatement.setLong(1, chatId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong("id");
                    } else {
                        throw new SQLException("INSERT into users returned no id for chat_id " + chatId);
                    }
                }
            }

        }
    }

    public int saveUserName(long chatId, String name) throws SQLException {
        long userId = findOrCreateUser(chatId);
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?")) {
            preparedStatement.setString(1, name);
            preparedStatement.setLong(2, userId);
            return preparedStatement.executeUpdate();

        }

    }

    public String getUserName(long chatId) throws SQLException {
        long userId = findOrCreateUser(chatId);
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM users WHERE id = ?")) {
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                } else {
                    throw new SQLException("select name query went wrong " + chatId);
                }
            }
        }
    }

    public int deleteUserName(long chatId) throws SQLException {
        Long existing = findUserId(chatId);
        if (existing != null) {
            try (Connection connection = database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET name = null WHERE id =?")) {
                preparedStatement.setLong(1, existing);
                return preparedStatement.executeUpdate();
            }

        } else {
            return 0;
        }
    }
}




