package de.tum.devops.application.dto;

import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * User data transfer object for embedded user information
 */
public class UserDto {

    @Nullable
    private UUID userID;
    private String fullName;
    private String email;
    private String role;

    // Constructors
    public UserDto() {
    }

    public UserDto(@Nullable UUID userID, String fullName, String email, String role) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    @Nullable
    public UUID getUserID() {
        return userID;
    }

    public void setUserID(@Nullable UUID userID) {
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
}