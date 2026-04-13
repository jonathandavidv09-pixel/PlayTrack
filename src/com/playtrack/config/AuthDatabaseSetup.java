package com.playtrack.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// System configuration component: manages database and app setup.
public class AuthDatabaseSetup {
    // setup.
    public static void setup() {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "username TEXT UNIQUE NOT NULL," +
                     "email TEXT UNIQUE NOT NULL," +
                     "password_hash TEXT NOT NULL," +
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                     ");";
                     
        String sqlOtpLogs = "CREATE TABLE IF NOT EXISTS otp_logs (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "email TEXT NOT NULL," +
                     "otp_code TEXT NOT NULL," +
                     "sent_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                     ");";
                     
        String sqlGlobalLogs = "CREATE TABLE IF NOT EXISTS global_logs (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "username TEXT NOT NULL," +
                     "action TEXT NOT NULL," +
                     "details TEXT," +
                     "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                     ");";

        try (Connection conn = AuthDBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlOtpLogs);
            stmt.execute(sqlGlobalLogs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}