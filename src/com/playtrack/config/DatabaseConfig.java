package com.playtrack.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseConfig {
    private static final String AUTH_DB_URL = "jdbc:sqlite:db/auth.db";
    private static final String SYSTEM_DB_URL = "jdbc:sqlite:db/playtrack.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            new File("db").mkdirs();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(AUTH_DB_URL);
    }

    public static Connection getSystemConnection() throws SQLException {
        return DriverManager.getConnection(SYSTEM_DB_URL);
    }

    public static void setupDatabases() {
        setupAuthDatabase();
        setupSystemDatabase();
    }

    private static void setupAuthDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "username TEXT UNIQUE NOT NULL," +
                     "email TEXT UNIQUE NOT NULL," +
                     "password_hash TEXT NOT NULL," +
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                     ");";
        try (Connection conn = getAuthConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupSystemDatabase() {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS profiles (" +
            "user_id INTEGER PRIMARY KEY," +
            "username TEXT NOT NULL," +
            "bio TEXT," +
            "avatar_path TEXT," +
            "joined_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");",
            
            "CREATE TABLE IF NOT EXISTS media_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "title TEXT NOT NULL," +
            "category TEXT NOT NULL," + // Film, Game, Book
            "genre TEXT," +
            "image_path TEXT," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");",
            
            "CREATE TABLE IF NOT EXISTS reviews (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "media_id INTEGER NOT NULL," +
            "user_id INTEGER NOT NULL," +
            "rating INTEGER," +
            "review_text TEXT," +
            "is_favorite BOOLEAN DEFAULT 0," +
            "review_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY(media_id) REFERENCES media_items(id)" +
            ");",
            
            "CREATE TABLE IF NOT EXISTS watchlist (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "title TEXT NOT NULL," +
            "category TEXT NOT NULL," +
            "added_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");",
            
            "CREATE TABLE IF NOT EXISTS activity_log (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "action TEXT NOT NULL," +
            "details TEXT," +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");"
        };

        try (Connection conn = getSystemConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
