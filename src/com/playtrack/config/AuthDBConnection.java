package com.playtrack.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

// Configuration component: opens connections to the authentication SQLite database.
public class AuthDBConnection {
    private static final String URL = "jdbc:sqlite:db/auth.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            new File("db").mkdirs();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Start: authentication database connection function.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    // End: authentication database connection function.
}
