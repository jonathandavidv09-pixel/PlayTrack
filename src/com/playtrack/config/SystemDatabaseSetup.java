package com.playtrack.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SystemDatabaseSetup {
    public static void setup() {
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
            "author TEXT," +
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
            ");",
            
            "CREATE TABLE IF NOT EXISTS favorites (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "media_id INTEGER NOT NULL," +
            "added_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY(media_id) REFERENCES media_items(id)" +
            ");"
        };

        try (Connection conn = SystemDBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
            
            // Safe automated migration
            try {
                stmt.execute("ALTER TABLE reviews ADD COLUMN is_watchlist BOOLEAN DEFAULT 0");
            } catch (SQLException ignore) {}
            try {
                stmt.execute("ALTER TABLE reviews ADD COLUMN watch_date TEXT");
            } catch (SQLException ignore) {}
            try {
                stmt.execute("ALTER TABLE media_items ADD COLUMN author TEXT");
            } catch (SQLException ignore) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
