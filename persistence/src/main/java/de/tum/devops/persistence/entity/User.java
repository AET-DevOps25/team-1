package de.tum.devops.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity corresponding to the 'users' table in init.sql
 * 
 * Table definition:
 * CREATE TABLE users (
 * user_id UUID PRIMARY KEY,
 * full_name VARCHAR(255) NOT NULL,
 * email VARCHAR(255) UNIQUE NOT NULL,
 * password_hash TEXT NOT NULL,
 * role user_role NOT NULL,
 * creation_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 * );
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    private UserRole role;

    @CreationTimestamp
    @Column(name = "creation_timestamp", updatable = false)
    private LocalDateTime creationTimestamp;

    // Constructors
    public User() {
        this.userId = UUID.randomUUID();
    }

    public User(String fullName, String email, String passwordHash, UserRole role) {
        this();
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", creationTimestamp=" + creationTimestamp +
                '}';
    }
}