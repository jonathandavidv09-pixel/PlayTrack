package com.playtrack.model;

import java.sql.Timestamp;

public class Review {
    private int id;
    private int mediaId;
    private int userId;
    private int rating;
    private String reviewText;
    private boolean isFavorite;
    private boolean isWatchlist;
    private Timestamp reviewDate;
    private String watchDate;

    public Review() {}

    public Review(int id, int mediaId, int userId, int rating, String reviewText, boolean isFavorite, boolean isWatchlist, String watchDate, Timestamp reviewDate) {
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

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMediaId() { return mediaId; }
    public void setMediaId(int mediaId) { this.mediaId = mediaId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public boolean isWatchlist() { return isWatchlist; }
    public void setWatchlist(boolean watchlist) { isWatchlist = watchlist; }
    public Timestamp getReviewDate() { return reviewDate; }
    public void setReviewDate(Timestamp reviewDate) { this.reviewDate = reviewDate; }
    public String getWatchDate() { return watchDate; }
    public void setWatchDate(String watchDate) { this.watchDate = watchDate; }
}
