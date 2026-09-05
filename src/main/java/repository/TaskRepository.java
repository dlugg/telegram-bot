package repository;

import model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import service.Database;

public class TaskRepository {
    private final Database database;
    private final UserRepository userRepository;

    public TaskRepository(Database database, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.database = database;
    }

    public void addTask(long chatId, String text) throws SQLException {

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId = userRepository.findOrCreateUser(connection, chatId);
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO tasks(user_id, task_text)  VALUES (?,?)")) {
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setString(2, text);
                    int inserted = preparedStatement.executeUpdate();
                    if (inserted < 1) {
                        throw new SQLException("INSERT into tasks affected 0 rows for chat_id " + chatId);
                    }
                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public List<Task> getTasks(long chatId) throws SQLException {
        List<Task> userTasks = new ArrayList<>();
        Long userId = userRepository.findUserId(chatId);
        if (userId == null) {
            return userTasks;
        }
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT task_text,is_done FROM tasks WHERE user_id = ? ORDER BY id"
             )) {
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userTasks.add(new Task(resultSet.getString("task_text"), resultSet.getBoolean("is_done")));
                }

            }
        }
        return userTasks;
    }


    public int clearAllTasks(long chatId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks WHERE user_id = (SELECT id FROM users WHERE chat_id = ?)")) {
            preparedStatement.setLong(1, chatId);
            return preparedStatement.executeUpdate();
        }

    }

    public Long findTaskIdByPosition(long chatId, int position) throws SQLException {
        if (position < 1) {
            return null;
        } else {
            try (Connection connection = database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM tasks " +
                         "WHERE user_id = (SELECT id FROM users WHERE chat_id = ? ) ORDER BY id LIMIT 1 OFFSET ?")) {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setInt(2, position - 1);
                try (
                        ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong("id");
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    public int removeTask(long chatId, int position) throws SQLException {
        Long taskId = findTaskIdByPosition(chatId, position);
        if (taskId == null) {
            return 0;
        }
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            preparedStatement.setLong(1, taskId);
            return preparedStatement.executeUpdate();
        }
    }

    public int markAsDone(long chatId, int position) throws SQLException {
        Long taskId = findTaskIdByPosition(chatId, position);
        if (taskId == null) {
            return 0;
        }
        try (Connection connection = database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tasks SET is_done = true WHERE id = ?")) {
            preparedStatement.setLong(1, taskId);
            return preparedStatement.executeUpdate();
        }
    }
}

