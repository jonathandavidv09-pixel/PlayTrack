package com.playtrack.model;

import java.sql.Timestamp;

// Domain model component: represents core application data.
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private Timestamp createdAt;

    // Constructor: initializes User.
    public User() {}

    // Constructor: initializes User.
    public User(int id, String username, String email, String passwordHash, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

   
    // getId.
    public int getId() { return id; }
    // setId.
    public void setId(int id) { this.id = id; }
    // getUsername.
    public String getUsername() { return username; }
    // setUsername.
    public void setUsername(String username) { this.username = username; }
    // getEmail.
    public String getEmail() { return email; }
    // setEmail.
    public void setEmail(String email) { this.email = email; }
    // getPasswordHash.
    public String getPasswordHash() { return passwordHash; }
    // setPasswordHash.
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    // getCreatedAt.
    public Timestamp getCreatedAt() { return createdAt; }
    // setCreatedAt.
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}