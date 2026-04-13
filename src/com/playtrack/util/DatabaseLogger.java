package com.playtrack.util;

import com.playtrack.config.AuthDBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Utility component: shared helpers for the system layer.
public class DatabaseLogger {

    // logActivity.
    public static void logActivity(String username, String action, String details) {
        String sql = "INSERT INTO global_logs (username, action, details) VALUES (?, ?, ?)";
        try (Connection conn = AuthDBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    // logOtp.
    public static void logOtp(String email, String otpCode) {
        String sql = "INSERT INTO otp_logs (email, otp_code) VALUES (?, ?)";
        try (Connection conn = AuthDBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, otpCode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log OTP: " + e.getMessage());
        }
    }
}