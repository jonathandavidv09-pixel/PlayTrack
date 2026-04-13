package com.playtrack.dao;

import com.playtrack.config.SystemDBConnection;
import com.playtrack.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data access component: handles persistence operations.
public class ReviewDAO {
    // addOrUpdateReview.
    public boolean addOrUpdateReview(Review review) {
        String checkSql = "SELECT id FROM reviews WHERE media_id = ? AND user_id = ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, review.getMediaId());
            checkStmt.setInt(2, review.getUserId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE reviews SET rating = ?, review_text = ?, is_favorite = ?, is_watchlist = ?, watch_date = ?, review_date = CURRENT_TIMESTAMP WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, review.getRating());
                        updateStmt.setString(2, review.getReviewText());
                        updateStmt.setBoolean(3, review.isFavorite());
                        updateStmt.setBoolean(4, review.isWatchlist());
                        updateStmt.setString(5, review.getWatchDate());
                        updateStmt.setInt(6, rs.getInt("id"));
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    String insertSql = "INSERT INTO reviews (media_id, user_id, rating, review_text, is_favorite, is_watchlist, watch_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, review.getMediaId());
                        insertStmt.setInt(2, review.getUserId());
                        insertStmt.setInt(3, review.getRating());
                        insertStmt.setString(4, review.getReviewText());
                        insertStmt.setBoolean(5, review.isFavorite());
                        insertStmt.setBoolean(6, review.isWatchlist());
                        insertStmt.setString(7, review.getWatchDate());
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // getReviewByMedia.
    public Review getReviewByMedia(int mediaId, int userId) {
        String sql = "SELECT * FROM reviews WHERE media_id = ? AND user_id = ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mediaId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Review(
                        rs.getInt("id"),
                        rs.getInt("media_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getBoolean("is_favorite"),
                        hasColumn(rs, "is_watchlist") && rs.getBoolean("is_watchlist"),
                        hasColumn(rs, "watch_date") ? rs.getString("watch_date") : null,
                        rs.getTimestamp("review_date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // hasColumn.
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    // getRecentReviews.
    public List<Review> getRecentReviews(int userId, int limit) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE user_id = ? ORDER BY review_date DESC LIMIT ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("media_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getBoolean("is_favorite"),
                        hasColumn(rs, "is_watchlist") && rs.getBoolean("is_watchlist"),
                        hasColumn(rs, "watch_date") ? rs.getString("watch_date") : null,
                        rs.getTimestamp("review_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // getFavorites.
    public List<Review> getFavorites(int userId, int limit) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE user_id = ? AND is_favorite = 1 ORDER BY review_date DESC LIMIT ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("media_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getBoolean("is_favorite"),
                        hasColumn(rs, "is_watchlist") && rs.getBoolean("is_watchlist"),
                        hasColumn(rs, "watch_date") ? rs.getString("watch_date") : null,
                        rs.getTimestamp("review_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // getWatchlists.
    public List<Review> getWatchlists(int userId, int limit) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE user_id = ? AND is_watchlist = 1 ORDER BY review_date DESC LIMIT ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                        rs.getInt("id"),
                        rs.getInt("media_id"),
                        rs.getInt("user_id"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getBoolean("is_favorite"),
                        hasColumn(rs, "is_watchlist") && rs.getBoolean("is_watchlist"),
                        hasColumn(rs, "watch_date") ? rs.getString("watch_date") : null,
                        rs.getTimestamp("review_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }
}