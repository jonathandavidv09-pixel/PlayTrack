package com.playtrack.service;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.dao.WatchlistDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import com.playtrack.model.WatchlistItem;
import java.util.List;

public class MediaService {
    private MediaDAO mediaDAO = new MediaDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    private WatchlistDAO watchlistDAO = new WatchlistDAO();

    public int addMedia(MediaItem item) {
        int id = mediaDAO.addMedia(item);
        if (id != -1) {
            String username = com.playtrack.util.SessionManager.getCurrentUser() != null 
                    ? com.playtrack.util.SessionManager.getCurrentUser().getUsername() : "Unknown";
            com.playtrack.util.DatabaseLogger.logActivity(username, "Added " + item.getCategory(), "Added " + item.getTitle());
        }
        return id;
    }

    public List<MediaItem> getMediaByUser(int userId, String category) {
        return mediaDAO.getMediaByUser(userId, category);
    }

    public List<MediaItem> searchMedia(int userId, String query) {
        return mediaDAO.searchMedia(userId, query);
    }

    public boolean updateMedia(MediaItem item) {
        return mediaDAO.updateMedia(item);
    }

    public boolean deleteMedia(int id) {
        return mediaDAO.deleteMedia(id);
    }

    public boolean addOrUpdateReview(Review review) {
        return reviewDAO.addOrUpdateReview(review);
    }

    public Review getReviewByMedia(int mediaId) {
        int userId = com.playtrack.util.SessionManager.getCurrentUser().getId();
        return reviewDAO.getReviewByMedia(mediaId, userId);
    }

    public boolean addToWatchlist(WatchlistItem item) {
        return watchlistDAO.addToWatchlist(item);
    }

    public List<WatchlistItem> getWatchlistByUser(int userId) {
        return watchlistDAO.getWatchlistByUser(userId);
    }

    public boolean removeFromWatchlist(int id) {
        return watchlistDAO.removeFromWatchlist(id);
    }
}
