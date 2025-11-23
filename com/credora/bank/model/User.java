package com.credora.bank.model;

import java.time.Instant;

public class User {

    private long id;
    private String username;
    private String passwordHash;   // maps to users.password_hash
    private String role;
    private Instant createdAt;

    public User() {
        // no-arg constructor (keep if you already had one)
    }

    // ✅ This is the constructor your DAO and AuthService need
    public User(long id, String username, String passwordHash, String role, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // getters & setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
