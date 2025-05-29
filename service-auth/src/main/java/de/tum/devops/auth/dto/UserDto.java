package de.tum.devops.auth.dto;

import de.tum.devops.persistence.entity.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User DTO according to api-documentation.yaml
 * 
 * Schema definition:
 * UserDto:
 * properties:
 * userID: string (uuid)
 * fullName: string (maxLength: 255)
 * email: string (email, maxLength: 255)
 * role: string (enum: [CANDIDATE, HR])
 * creationTimestamp: string (date-time)
 */
public class UserDto {

    private UUID userID;
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDateTime creationTimestamp;

    // Constructors
    public UserDto() {
    }

    public UserDto(UUID userID, String fullName, String email, UserRole role, LocalDateTime creationTimestamp) {
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
}