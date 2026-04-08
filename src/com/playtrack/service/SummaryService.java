package com.playtrack.service;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SummaryService {
    private MediaDAO mediaDAO = new MediaDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();

    public Map<String, Integer> getCategoryCounts(int userId) {
        List<MediaItem> items = mediaDAO.getMediaByUser(userId, "All");
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Films", 0);
        counts.put("Games", 0);
        counts.put("Books", 0);
        for (MediaItem item : items) {
            counts.put(item.getCategory(), counts.getOrDefault(item.getCategory(), 0) + 1);
        }
        return counts;
    }

    public List<Review> getRecentActivity(int userId, int limit) {
        return reviewDAO.getRecentReviews(userId, limit);
    }

    public double getAverageRating(int userId) {
        List<Review> reviews = reviewDAO.getRecentReviews(userId, 1000);
        // Batch fetch all media IDs to avoid N+1 DB queries
        java.util.Set<Integer> validMediaIds = mediaDAO.getMediaByUser(userId, "All").stream()
            .map(MediaItem::getId)
            .collect(java.util.stream.Collectors.toSet());
        List<Review> validReviews = reviews.stream()
            .filter(r -> r.getRating() > 0)
            .filter(r -> validMediaIds.contains(r.getMediaId()))
            .collect(Collectors.toList());
            
        if (validReviews.isEmpty()) return 0.0;
        return validReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    public Map<String, Integer> getTopGenres(int userId, String category) {
        List<MediaItem> items = mediaDAO.getMediaByUser(userId, category);
        Map<String, Integer> genres = new HashMap<>();
        for (MediaItem item : items) {
            if (item.getGenre() != null && !item.getGenre().isEmpty()) {
                genres.put(item.getGenre(), genres.getOrDefault(item.getGenre(), 0) + 1);
            }
        }
        return genres.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, java.util.LinkedHashMap::new));
    }

    public static class RatedMedia {
        public MediaItem media;
        public int rating;
        public RatedMedia(MediaItem media, int rating) { 
            this.media = media; 
            this.rating = rating; 
        }
    }

    public List<RatedMedia> getTopRatedMedia(int userId, int limit) {
        List<Review> allReviews = reviewDAO.getRecentReviews(userId, 1000);
        // Batch fetch all media items to avoid N+1 DB queries
        java.util.Map<Integer, MediaItem> mediaMap = mediaDAO.getMediaByUser(userId, "All").stream()
            .collect(java.util.stream.Collectors.toMap(MediaItem::getId, m -> m, (a, b) -> a));
        return allReviews.stream()
            .filter(r -> r.getRating() > 0)
            .sorted((r1, r2) -> Integer.compare(r2.getRating(), r1.getRating()))
            .map(r -> new RatedMedia(mediaMap.get(r.getMediaId()), r.getRating()))
            .filter(rm -> rm.media != null)
            .limit(limit)
            .collect(Collectors.toList());
    }
}
