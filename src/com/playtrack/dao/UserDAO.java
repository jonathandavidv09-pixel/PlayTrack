package com.playtrack.dao;

import com.playtrack.config.AuthDBConnection;
import com.playtrack.model.User;
import java.sql.*;

// Data access component: reads and writes authentication user records.
public class UserDAO {
    // Start: register user database function.
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = AuthDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // End: register user database function.

    // Start: login lookup database function.
    public User login(String identifier, String passwordHash) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND password_hash = ?";
        try (Connection conn = AuthDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            pstmt.setString(3, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // End: login lookup database function.

    // Start: username availability database function.
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = AuthDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // End: username availability database function.

    // Start: load user by id database function.
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = AuthDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // End: load user by id database function.

    // Start: update user account database function.
    public boolean updateUserAndAuth(int userId, String newUsername, String newEmail, String newPasswordHash) {
        String baseSql = "UPDATE users SET username = ?, email = ? ";
        boolean updatePassword = (newPasswordHash != null && !newPasswordHash.isEmpty());
        if (updatePassword) {
            baseSql += ", password_hash = ? ";
        }
        baseSql += "WHERE id = ?";
        
        try (Connection conn = AuthDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(baseSql)) {
             
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newEmail);
            
            int paramIndex = 3;
            if (updatePassword) {
                pstmt.setString(paramIndex++, newPasswordHash);
            }
            pstmt.setInt(paramIndex, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // End: update user account database function.
}
