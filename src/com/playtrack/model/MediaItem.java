package com.playtrack.model;

import java.sql.Timestamp;

public class MediaItem {
    private int id;
    private int userId;
    private String title;
    private String category; 
    private String genre;
    private String author;
    private String imagePath;
    private Timestamp createdAt;

    public MediaItem() {}

    public MediaItem(int id, int userId, String title, String category, String genre, String author, String imagePath, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.genre = genre;
        this.author = author;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
