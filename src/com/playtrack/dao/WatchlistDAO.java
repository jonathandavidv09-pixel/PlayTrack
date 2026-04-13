package com.playtrack.dao;

import com.playtrack.config.SystemDBConnection;
import com.playtrack.model.WatchlistItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data access component: handles persistence operations.
public class WatchlistDAO {
    // addToWatchlist.
    public boolean addToWatchlist(WatchlistItem item) {
        String sql = "INSERT INTO watchlist (user_id, title, category) VALUES (?, ?, ?)";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getUserId());
            pstmt.setString(2, item.getTitle());
            pstmt.setString(3, item.getCategory());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // getWatchlistByUser.
    public List<WatchlistItem> getWatchlistByUser(int userId) {
        List<WatchlistItem> items = new ArrayList<>();
        String sql = "SELECT * FROM watchlist WHERE user_id = ? ORDER BY added_date DESC";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new WatchlistItem(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getTimestamp("added_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // removeFromWatchlist.
    public boolean removeFromWatchlist(int id) {
        String sql = "DELETE FROM watchlist WHERE id = ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}