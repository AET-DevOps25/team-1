package de.tum.devops.job.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User data transfer object for embedded user information
 */
public class UserDto {

    private UUID userID;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime creationTimestamp;

    // Constructors
    public UserDto() {
    }

    public UserDto(UUID userID, String fullName, String email, String role, LocalDateTime creationTimestamp) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.creationTimestamp = creationTimestamp;
    }

    // Getters and Setters
    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}