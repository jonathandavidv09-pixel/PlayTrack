package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents core application data.
public class Profile {
    private int userId;
    private String username;
    private String bio;
    private String avatarPath;
    private Timestamp joinedDate;

    // Constructor: initializes Profile.
    public Profile() {}

    // Constructor: initializes Profile.
    public Profile(int userId, String username, String bio, String avatarPath, Timestamp joinedDate) {
        this.userId = userId;
        this.username = username;
        this.bio = bio;
        this.avatarPath = avatarPath;
        this.joinedDate = joinedDate;
    }

    
    // getUserId.
    public int getUserId() { return userId; }
    // setUserId.
    public void setUserId(int userId) { this.userId = userId; }
    // getUsername.
    public String getUsername() { return username; }
    // setUsername.
    public void setUsername(String username) { this.username = username; }
    // getBio.
    public String getBio() { return bio; }
    // setBio.
    public void setBio(String bio) { this.bio = bio; }
    // getAvatarPath.
    public String getAvatarPath() { return avatarPath; }
    // setAvatarPath.
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    // getJoinedDate.
    public Timestamp getJoinedDate() { return joinedDate; }
    // setJoinedDate.
    public void setJoinedDate(Timestamp joinedDate) { this.joinedDate = joinedDate; }
}