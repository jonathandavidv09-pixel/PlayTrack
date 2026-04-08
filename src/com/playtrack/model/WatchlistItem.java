package com.playtrack.model;

import java.sql.Timestamp;

public class WatchlistItem {
    private int id;
    private int userId;
    private String title;
    private String category;
    private Timestamp addedDate;

    public WatchlistItem() {}

    public WatchlistItem(int id, int userId, String title, String category, Timestamp addedDate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.addedDate = addedDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Timestamp getAddedDate() { return addedDate; }
    public void setAddedDate(Timestamp addedDate) { this.addedDate = addedDate; }
}
