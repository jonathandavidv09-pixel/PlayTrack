package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents core application data.
public class MediaItem {
    private int id;
    private int userId;
    private String title;
    private String category; 
    private String genre;
    private String author;
    private String imagePath;
    private Timestamp createdAt;

    // Constructor: initializes MediaItem.
    public MediaItem() {}

    // Constructor: initializes MediaItem.
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
    // getGenre.
    public String getGenre() { return genre; }
    // setGenre.
    public void setGenre(String genre) { this.genre = genre; }
    // getAuthor.
    public String getAuthor() { return author; }
    // setAuthor.
    public void setAuthor(String author) { this.author = author; }
    // getImagePath.
    public String getImagePath() { return imagePath; }
    // setImagePath.
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    // getCreatedAt.
    public Timestamp getCreatedAt() { return createdAt; }
    // setCreatedAt.
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}