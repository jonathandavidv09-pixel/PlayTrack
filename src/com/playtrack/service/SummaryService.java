package com.playtrack.service;

import com.playtrack.dao.MediaDAO;
import com.playtrack.dao.ReviewDAO;
import com.playtrack.model.MediaItem;
import com.playtrack.model.Review;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

// Service layer component: builds dashboard summaries from media and review data.
public class SummaryService {
    private MediaDAO mediaDAO = new MediaDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();

    // Start: category count summary function.
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
    // End: category count summary function.

    // Start: recent activity summary function.
    public List<Review> getRecentActivity(int userId, int limit) {
        return reviewDAO.getRecentReviews(userId, limit);
    }
    // End: recent activity summary function.

    // Start: average rating summary function.
    public double getAverageRating(int userId) {
        List<Review> reviews = reviewDAO.getRecentReviews(userId, Integer.MAX_VALUE);
     
        java.util.Set<Integer> validMediaIds = mediaDAO.getMediaByUser(userId, "All").stream()
            .map(MediaItem::getId)
            .collect(java.util.stream.Collectors.toSet());
        List<Review> validReviews = reviews.stream()
            .filter(r -> r.getRating() > 0)
            .filter(r -> validMediaIds.contains(r.getMediaId()))
            .collect(Collectors.toList());
            
        if (validReviews.isEmpty()) return 0.0;
        return validReviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
    }
    // End: average rating summary function.

    // Start: top genres summary function.
    public Map<String, Integer> getTopGenres(int userId, String category) {
        return getGenreDistribution(userId, category, 5, false);
    }
    // End: top genres summary function.

    // Start: genre distribution summary function.
    public Map<String, Integer> getGenreDistribution(int userId, String category, int topLimit, boolean includeOther) {
        List<MediaItem> items = mediaDAO.getMediaByUser(userId, category);
        Map<String, Integer> genres = new HashMap<>();
        for (MediaItem item : items) {
            String genre = extractPrimaryGenre(item.getGenre());
            genres.put(genre, genres.getOrDefault(genre, 0) + 1);
        }

        if (genres.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map.Entry<String, Integer>> sorted = genres.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        int safeTopLimit = Math.max(1, topLimit);
        Map<String, Integer> out = new LinkedHashMap<>();
        int otherCount = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, Integer> e = sorted.get(i);
            if (i < safeTopLimit) {
                out.put(e.getKey(), e.getValue());
            } else {
                otherCount += e.getValue();
            }
        }
        if (includeOther && otherCount > 0) {
            out.put("Other", otherCount);
        }
        return out;
    }
    // End: genre distribution summary function.

    // Start: primary genre extraction helper function.
    private String extractPrimaryGenre(String rawGenre) {
        if (rawGenre == null) {
            return "Other";
        }
        String[] parts = rawGenre.split("[,;/]");
        for (String part : parts) {
            String normalized = part == null ? "" : part.trim();
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return "Other";
    }
    // End: primary genre extraction helper function.

    // Start: rated media summary data holder.
    public static class RatedMedia {
        public MediaItem media;
        public double rating;
        public RatedMedia(MediaItem media, double rating) { 
            this.media = media; 
            this.rating = rating; 
        }
    }
    // End: rated media summary data holder.

    // Start: top rated media summary function.
    public List<RatedMedia> getTopRatedMedia(int userId, int limit) {
        List<Review> allReviews = reviewDAO.getRecentReviews(userId, Integer.MAX_VALUE);
        
        java.util.Map<Integer, MediaItem> mediaMap = mediaDAO.getMediaByUser(userId, "All").stream()
            .collect(java.util.stream.Collectors.toMap(MediaItem::getId, m -> m, (a, b) -> a));
        return allReviews.stream()
            .filter(r -> r.getRating() > 0)
            .sorted((r1, r2) -> Double.compare(r2.getRating(), r1.getRating()))
            .map(r -> new RatedMedia(mediaMap.get(r.getMediaId()), r.getRating()))
            .filter(rm -> rm.media != null)
            .limit(limit)
            .collect(Collectors.toList());
    }
    // End: top rated media summary function.

    // Start: activity heatmap summary function.
    public int[][] getActivityHeatmapByDay(int userId, int weeks) {
        int safeWeeks = Math.max(1, weeks);
        int[][] heat = new int[safeWeeks][7];

        List<Review> reviews = reviewDAO.getRecentReviews(userId, Integer.MAX_VALUE);
        if (reviews.isEmpty()) {
            return heat;
        }

        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate oldestWeekStart = currentWeekStart.minusWeeks(safeWeeks - 1);

        for (Review review : reviews) {
            LocalDate activityDate = resolveActivityDate(review);
            if (activityDate == null) {
                continue;
            }
            if (activityDate.isBefore(oldestWeekStart) || activityDate.isAfter(today)) {
                continue;
            }

            LocalDate weekStart = activityDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            long weekOffset = ChronoUnit.WEEKS.between(oldestWeekStart, weekStart);
            if (weekOffset < 0 || weekOffset >= safeWeeks) {
                continue;
            }

            int dayIndex = activityDate.getDayOfWeek().getValue() - 1; // Monday=0 ... Sunday=6
            heat[(int) weekOffset][dayIndex]++;
        }

        return heat;
    }
    // End: activity heatmap summary function.

    // Start: review activity date resolver helper function.
    private LocalDate resolveActivityDate(Review review) {
        String watchDate = review.getWatchDate();
        if (watchDate != null && !watchDate.trim().isEmpty()) {
            try {
                return LocalDate.parse(watchDate.trim());
            } catch (Exception ignored) {
            }
        }
        if (review.getReviewDate() != null) {
            return review.getReviewDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }
    // End: review activity date resolver helper function.
}
