package de.tum.devops.auth.dto;

/**
 * Authentication response DTO according to api-documentation.yaml
 * 
 * Schema definition:
 * AuthResponse:
 * properties:
 * accessToken: string
 * refreshToken: string
 * user: UserDto
 * expiresIn: integer
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private int expiresIn;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, UserDto user, int expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}