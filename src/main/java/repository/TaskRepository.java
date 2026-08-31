package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {


    public static void main(String[] args) throws SQLException {
        TaskRepository user = new TaskRepository();
        System.out.println(user.getTasks(11323123));
    }

    private static final String PASSWORD = System.getenv("DATABASE_PASSWORD");
    private static final String URL = "jdbc:postgresql://localhost:5432/javabot";
    private static final String USER = "postgres";


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public Long findUserId(long chatId) throws SQLException {
        try (Connection connection = getConnection();
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
            try (Connection connection = getConnection();
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

    public void addTask(long chatId, String text) throws SQLException {
        long userId = findOrCreateUser(chatId);
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO tasks(user_id, task_text)  VALUES (?,?)")
        ) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, text);
            int inserted = preparedStatement.executeUpdate();
            if (inserted < 1) {
                throw new SQLException("INSERT into tasks affected 0 rows for chat_id " + chatId);
            }
        }
    }

    public List<String> getTasks(long chatId) throws SQLException {
        List<String> userTasks = new ArrayList<>();
        Long userId = findUserId(chatId);
        if (userId == null) {
            return userTasks;
        }
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT task_text FROM tasks WHERE user_id = ?"
             )) {
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userTasks.add(resultSet.getString("task_text"));
                }
            }
        }
        return userTasks;
    }
}