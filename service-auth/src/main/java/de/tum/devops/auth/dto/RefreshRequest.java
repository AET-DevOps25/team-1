package de.tum.devops.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token request DTO according to api-documentation.yaml
 * 
 * Schema definition:
 * RefreshRequest:
 * properties:
 * refreshToken: string
 */
public class RefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Constructors
    public RefreshRequest() {
    }

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}