package com.playtrack.service;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.dao.WatchlistDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import com.playtrack.model.WatchlistItem;
import java.util.List;

// Service layer component: coordinates business logic.
public class MediaService {
    private MediaDAO mediaDAO = new MediaDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    private WatchlistDAO watchlistDAO = new WatchlistDAO();

    // addMedia.
    public int addMedia(MediaItem item) {
        int id = mediaDAO.addMedia(item);
        if (id != -1) {
            String username = com.playtrack.util.SessionManager.getCurrentUser() != null 
                    ? com.playtrack.util.SessionManager.getCurrentUser().getUsername() : "Unknown";
            com.playtrack.util.DatabaseLogger.logActivity(username, "Added " + item.getCategory(), "Added " + item.getTitle());
        }
        return id;
    }

    // getMediaByUser.
    public List<MediaItem> getMediaByUser(int userId, String category) {
        return mediaDAO.getMediaByUser(userId, category);
    }

    // searchMedia.
    public List<MediaItem> searchMedia(int userId, String query) {
        return mediaDAO.searchMedia(userId, query);
    }

    // updateMedia.
    public boolean updateMedia(MediaItem item) {
        return mediaDAO.updateMedia(item);
    }

    // deleteMedia.
    public boolean deleteMedia(int id) {
        return mediaDAO.deleteMedia(id);
    }

    // addOrUpdateReview.
    public boolean addOrUpdateReview(Review review) {
        return reviewDAO.addOrUpdateReview(review);
    }

    // getReviewByMedia.
    public Review getReviewByMedia(int mediaId) {
        int userId = com.playtrack.util.SessionManager.getCurrentUser().getId();
        return reviewDAO.getReviewByMedia(mediaId, userId);
    }

    // addToWatchlist.
    public boolean addToWatchlist(WatchlistItem item) {
        return watchlistDAO.addToWatchlist(item);
    }

    // getWatchlistByUser.
    public List<WatchlistItem> getWatchlistByUser(int userId) {
        return watchlistDAO.getWatchlistByUser(userId);
    }

    // removeFromWatchlist.
    public boolean removeFromWatchlist(int id) {
        return watchlistDAO.removeFromWatchlist(id);
    }
}