package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents core application data.
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

    // Constructor: initializes Review.
    public Review() {}

    // Constructor: initializes Review.
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
    public int getRating() { return rating; }
    // setRating.
    public void setRating(int rating) { this.rating = rating; }
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
}