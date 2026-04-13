package com.playtrack.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

// System configuration component: manages database and app setup.
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

    // getConnection.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}