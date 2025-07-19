package de.tum.devops.job.dto;

import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * User data transfer object for embedded user information
 */
public class UserDto {

    @Nullable
    private UUID userId;
    private String fullName;
    private String email;
    private String role;

    // Constructors
    public UserDto() {
    }

    public UserDto(@Nullable UUID userId, String fullName, String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    @Nullable
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(@Nullable UUID userId) {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}