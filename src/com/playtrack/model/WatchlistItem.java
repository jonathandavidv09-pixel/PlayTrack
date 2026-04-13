package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents core application data.
public class WatchlistItem {
    private int id;
    private int userId;
    private String title;
    private String category;
    private Timestamp addedDate;

    // Constructor: initializes WatchlistItem.
    public WatchlistItem() {}

    // Constructor: initializes WatchlistItem.
    public WatchlistItem(int id, int userId, String title, String category, Timestamp addedDate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.addedDate = addedDate;
    }

    
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
}