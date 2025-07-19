package de.tum.devops.auth.dto;

import de.tum.devops.auth.persistence.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User DTO according to api-openapi-original-design.yaml
 * <p>
 * Schema definition:
 * UserDto:
 * properties:
 * userId: string (uuid)
 * fullName: string (maxLength: 255)
 * email: string (email, maxLength: 255)
 * role: string (enum: [CANDIDATE, HR])
 * creationTimestamp: string (date-time)
 */
public class UserDto {

    private UUID userId;
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    // Constructors
    public UserDto() {
    }

    public UserDto(UUID userId, String fullName, String email, UserRole role, LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}