package com.playtrack.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

// Configuration component: opens connections to the main PlayTrack SQLite database.
public class SystemDBConnection {
    private static final String URL = "jdbc:sqlite:db/playtrack.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            new File("db").mkdirs();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Start: system database connection function.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    // End: system database connection function.
}
