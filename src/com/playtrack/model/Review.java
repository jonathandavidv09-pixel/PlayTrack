package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents a user's rating, notes, and saved state for media.
public class Review {
    // Start: review data fields.
    private int id;
    private int mediaId;
    private int userId;
    private double rating;
    private String reviewText;
    private boolean isFavorite;
    private boolean isWatchlist;
    private Timestamp reviewDate;
    private String watchDate;
    // End: review data fields.

    // Start: empty review constructor.
    public Review() {}
    // End: empty review constructor.

    // Start: full review constructor.
    public Review(int id, int mediaId, int userId, double rating, String reviewText, boolean isFavorite, boolean isWatchlist, String watchDate, Timestamp reviewDate) {
        this.id = id;
        this.mediaId = mediaId;
        this.userId = userId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.isFavorite = isFavorite;
        this.isWatchlist = isWatchlist;
        this.watchDate = watchDate;
        this.reviewDate = reviewDate;
    }
    // End: full review constructor.

    
    // Start: review getters and setters.
    // getId.
    public int getId() { return id; }
    // setId.
    public void setId(int id) { this.id = id; }
    // getMediaId.
    public int getMediaId() { return mediaId; }
    // setMediaId.
    public void setMediaId(int mediaId) { this.mediaId = mediaId; }
    // getUserId.
    public int getUserId() { return userId; }
    // setUserId.
    public void setUserId(int userId) { this.userId = userId; }
    // getRating.
    public double getRating() { return rating; }
    // setRating.
    public void setRating(double rating) { this.rating = rating; }
    // getReviewText.
    public String getReviewText() { return reviewText; }
    // setReviewText.
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    // isFavorite.
    public boolean isFavorite() { return isFavorite; }
    // setFavorite.
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    // isWatchlist.
    public boolean isWatchlist() { return isWatchlist; }
    // setWatchlist.
    public void setWatchlist(boolean watchlist) { isWatchlist = watchlist; }
    // getReviewDate.
    public Timestamp getReviewDate() { return reviewDate; }
    // setReviewDate.
    public void setReviewDate(Timestamp reviewDate) { this.reviewDate = reviewDate; }
    // getWatchDate.
    public String getWatchDate() { return watchDate; }
    // setWatchDate.
    public void setWatchDate(String watchDate) { this.watchDate = watchDate; }
    // End: review getters and setters.
}
