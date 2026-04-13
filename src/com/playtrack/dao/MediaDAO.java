package com.playtrack.dao;

import com.playtrack.config.SystemDBConnection;
import com.playtrack.model.MediaItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data access component: handles persistence operations.
public class MediaDAO {
    // addMedia.
    public int addMedia(MediaItem item) {
        String sql = "INSERT INTO media_items (user_id, title, category, genre, author, image_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getUserId());
            pstmt.setString(2, item.getTitle());
            pstmt.setString(3, item.getCategory());
            pstmt.setString(4, item.getGenre());
            pstmt.setString(5, item.getAuthor());
            pstmt.setString(6, item.getImagePath());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // getMediaByUser.
    public List<MediaItem> getMediaByUser(int userId, String category) {
        List<MediaItem> items = new ArrayList<>();
        String sql = "SELECT * FROM media_items WHERE user_id = ?";
        if (category != null && !category.equals("All")) {
            sql += " AND category = ?";
        }
        sql += " ORDER BY created_at DESC";

        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            if (category != null && !category.equals("All")) {
                pstmt.setString(2, category);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new MediaItem(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("genre"),
                        rs.getString("author"),
                        rs.getString("image_path"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // getMediaById.
    public MediaItem getMediaById(int id) {
        String sql = "SELECT * FROM media_items WHERE id = ?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new MediaItem(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("genre"),
                        rs.getString("author"),
                        rs.getString("image_path"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // searchMedia.
    public List<MediaItem> searchMedia(int userId, String query) {
        List<MediaItem> items = new ArrayList<>();
        String sql = "SELECT * FROM media_items WHERE user_id = ? AND title LIKE ? ORDER BY created_at DESC";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new MediaItem(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("genre"),
                        rs.getString("author"),
                        rs.getString("image_path"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // updateMedia.
    public boolean updateMedia(MediaItem item) {
        String sql = "UPDATE media_items SET title=?, category=?, genre=?, author=?, image_path=? WHERE id=?";
        try (Connection conn = SystemDBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getTitle());
            pstmt.setString(2, item.getCategory());
            pstmt.setString(3, item.getGenre());
            pstmt.setString(4, item.getAuthor());
            pstmt.setString(5, item.getImagePath());
            pstmt.setInt(6, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // deleteMedia.
    public boolean deleteMedia(int id) {
       
        String deleteReviews = "DELETE FROM reviews WHERE media_id = ?";
        String deleteMedia = "DELETE FROM media_items WHERE id = ?";
        try (Connection conn = SystemDBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(deleteReviews)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMedia)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}