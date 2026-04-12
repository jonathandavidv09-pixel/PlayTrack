package com.playtrack.model;

import java.sql.Timestamp;

public class Profile {
    private int userId;
    private String username;
    private String bio;
    private String avatarPath;
    private Timestamp joinedDate;

    public Profile() {}

    public Profile(int userId, String username, String bio, String avatarPath, Timestamp joinedDate) {
        this.userId = userId;
        this.username = username;
        this.bio = bio;
        this.avatarPath = avatarPath;
        this.joinedDate = joinedDate;
    }

    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public Timestamp getJoinedDate() { return joinedDate; }
    public void setJoinedDate(Timestamp joinedDate) { this.joinedDate = joinedDate; }
}
