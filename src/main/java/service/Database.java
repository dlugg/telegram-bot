package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String PASSWORD = System.getenv("DATABASE_PASSWORD");
    private static final String URL = "jdbc:postgresql://localhost:5432/javabot";
    private static final String USER = "postgres";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
