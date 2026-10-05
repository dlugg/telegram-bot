package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class Database {
    private final String url;
    private final String user;
    private final String password;

    public Database() {
        this(Optional.ofNullable(System.getenv("DATABASE_URL")).orElse("jdbc:postgresql://localhost:5432/javabot"),
                "postgres",
                System.getenv("DATABASE_PASSWORD"));
    }

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}