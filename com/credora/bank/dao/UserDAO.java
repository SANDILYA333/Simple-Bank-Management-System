package com.credora.bank.dao;

import java.sql.Connection;
import java.time.Instant;

import com.credora.bank.model.User;

public class UserDAO {

    private long id;
    private String username;
    private String passwordHash;   // maps to users.password_hash
    private String role;
    private Instant createdAt;

    public UserDAO() {
        // no-arg constructor (keep if you already had one)
    }

    // ✅ This is the constructor your DAO and AuthService need
    public UserDAO(long id, String username, String passwordHash, String role, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // getters & setters

    public UserDAO(Connection conn) {
        //TODO Auto-generated constructor stub
    }

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

    public User findByUsername(String username2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByUsername'");
    }

    public User create(String username2, String hash, String role2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }
}
