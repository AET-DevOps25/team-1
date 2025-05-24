package com.aihr.service.gateway.dto;

import com.aihr.service.gateway.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for User information in API responses
 */
public class UserDto {

    private UUID userID;
    private String fullName;
    private String email;
    private UserRole role;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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

    // Static factory method to create from User entity
    public static UserDto fromUser(com.aihr.service.gateway.entity.User user) {
        return new UserDto(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreationTimestamp());
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

    @Override
    public String toString() {
        return "UserDto{" +
                "userID=" + userID +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", creationTimestamp=" + creationTimestamp +
                '}';
    }
}