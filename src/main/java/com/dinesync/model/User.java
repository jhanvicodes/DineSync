package com.dinesync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * User model — represents a row in the 'users' table.
 * A user can be a CUSTOMER (books tables) or ADMIN (manages the system).
 */
public class User {

    private Integer userId;
    private String name;
    private String email;
    private String phone;

    @JsonIgnore
    private String password;      // Stored as BCrypt hash — never serialized to JSON
    private String role;          // "CUSTOMER" or "ADMIN"
    private LocalDateTime createdAt;

    // Default constructor (required by JdbcTemplate RowMapper)
    public User() {}

    // Constructor for creating a new user (without ID, which DB auto-generates)
    public User(String name, String email, String phone, String password, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    // --- Getters and Setters ---

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
