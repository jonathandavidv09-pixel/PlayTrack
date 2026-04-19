package com.playtrack.service;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.dao.WatchlistDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import com.playtrack.model.WatchlistItem;
import java.util.List;

// Service layer component: coordinates media, review, and watchlist business logic.
public class MediaService {
    private MediaDAO mediaDAO = new MediaDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    private WatchlistDAO watchlistDAO = new WatchlistDAO();

    // Start: add media service function.
    public int addMedia(MediaItem item) {
        int id = mediaDAO.addMedia(item);
        if (id != -1) {
            String username = com.playtrack.util.SessionManager.getCurrentUser() != null 
                    ? com.playtrack.util.SessionManager.getCurrentUser().getUsername() : "Unknown";
            com.playtrack.util.DatabaseLogger.logActivity(username, "Added " + item.getCategory(), "Added " + item.getTitle());
        }
        return id;
    }
    // End: add media service function.

    // Start: load media by user service function.
    public List<MediaItem> getMediaByUser(int userId, String category) {
        return mediaDAO.getMediaByUser(userId, category);
    }
    // End: load media by user service function.

    // Start: search media service function.
    public List<MediaItem> searchMedia(int userId, String query) {
        return mediaDAO.searchMedia(userId, query);
    }
    // End: search media service function.

    // Start: update media service function.
    public boolean updateMedia(MediaItem item) {
        return mediaDAO.updateMedia(item);
    }
    // End: update media service function.

    // Start: delete media service function.
    public boolean deleteMedia(int id) {
        return mediaDAO.deleteMedia(id);
    }
    // End: delete media service function.

    // Start: add or update review service function.
    public boolean addOrUpdateReview(Review review) {
        return reviewDAO.addOrUpdateReview(review);
    }
    // End: add or update review service function.

    // Start: load current user's review service function.
    public Review getReviewByMedia(int mediaId) {
        int userId = com.playtrack.util.SessionManager.getCurrentUser().getId();
        return reviewDAO.getReviewByMedia(mediaId, userId);
    }
    // End: load current user's review service function.

    // Start: add watchlist item service function.
    public boolean addToWatchlist(WatchlistItem item) {
        return watchlistDAO.addToWatchlist(item);
    }
    // End: add watchlist item service function.

    // Start: load watchlist by user service function.
    public List<WatchlistItem> getWatchlistByUser(int userId) {
        return watchlistDAO.getWatchlistByUser(userId);
    }
    // End: load watchlist by user service function.

    // Start: remove watchlist item service function.
    public boolean removeFromWatchlist(int id) {
        return watchlistDAO.removeFromWatchlist(id);
    }
    // End: remove watchlist item service function.
}
