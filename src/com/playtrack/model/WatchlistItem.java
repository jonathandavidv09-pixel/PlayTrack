package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents one item saved to a user's watchlist.
public class WatchlistItem {
    // Start: watchlist item data fields.
    private int id;
    private int userId;
    private String title;
    private String category;
    private Timestamp addedDate;
    // End: watchlist item data fields.

    // Start: empty watchlist item constructor.
    public WatchlistItem() {}
    // End: empty watchlist item constructor.

    // Start: full watchlist item constructor.
    public WatchlistItem(int id, int userId, String title, String category, Timestamp addedDate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.addedDate = addedDate;
    }
    // End: full watchlist item constructor.

    
    // Start: watchlist item getters and setters.
    // getId.
    public int getId() { return id; }
    // setId.
    public void setId(int id) { this.id = id; }
    // getUserId.
    public int getUserId() { return userId; }
    // setUserId.
    public void setUserId(int userId) { this.userId = userId; }
    // getTitle.
    public String getTitle() { return title; }
    // setTitle.
    public void setTitle(String title) { this.title = title; }
    // getCategory.
    public String getCategory() { return category; }
    // setCategory.
    public void setCategory(String category) { this.category = category; }
    // getAddedDate.
    public Timestamp getAddedDate() { return addedDate; }
    // setAddedDate.
    public void setAddedDate(Timestamp addedDate) { this.addedDate = addedDate; }
    // End: watchlist item getters and setters.
}
