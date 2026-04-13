package com.playtrack.dao;

import com.playtrack.config.SystemDBConnection;
import com.playtrack.model.Profile;
import java.sql.*;

// Data access component: handles persistence operations.
public class ProfileDAO {
    // createProfile.
    public boolean createProfile(Profile profile) {
        String sql = "INSERT INTO profiles (user_id, username, bio, avatar_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, profile.getUserId());
            pstmt.setString(2, profile.getUsername());
            pstmt.setString(3, profile.getBio());
            pstmt.setString(4, profile.getAvatarPath());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // getProfile.
    public Profile getProfile(int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Profile(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("bio"),
                        rs.getString("avatar_path"),
                        rs.getTimestamp("joined_date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // updateProfile.
    public boolean updateProfile(Profile profile) {
        String sql = "UPDATE profiles SET username = ?, bio = ?, avatar_path = ? WHERE user_id = ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, profile.getUsername());
            pstmt.setString(2, profile.getBio());
            pstmt.setString(3, profile.getAvatarPath());
            pstmt.setInt(4, profile.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}