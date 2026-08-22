import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbTest {

    private static final String PASSWORD = System.getenv("DATABASE_PASSWORD");
    private static final String URL = "jdbc:postgresql://localhost:5432/javabot";
    private static final String USER = "postgres";

    public static void main(String[] args) throws SQLException {
        System.out.println(deleteTask(1));
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static List<String> findTasks(long userId) throws SQLException {

        List<String> userTasks = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT task_text FROM tasks WHERE user_id=?")) {
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userTasks.add(resultSet.getString("task_text"));
                }
            }
        }
        return userTasks;
    }

    public static int addTask(long userId, String text) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tasks(user_id, task_text)  VALUES (?,?)")) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, text);
            return preparedStatement.executeUpdate();
        }
    }

    public static int deleteTask(long taskId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks where tasks.id=?")) {
            preparedStatement.setLong(1, taskId);
            return preparedStatement.executeUpdate();
        }
    }
}
