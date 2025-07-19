package de.tum.devops.auth.dto;

/**
 * Authentication response DTO according to api-openapi-original-design.yaml
 * <p>
 * Schema definition:
 * AuthResponse:
 * properties:
 * accessToken: string
 * user: UserDto
 * expiresIn: integer
 */
public class AuthResponse {

    private String accessToken;
    private UserDto user;
    private int expiresIn;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String accessToken, UserDto user, int expiresIn) {
        this.accessToken = accessToken;
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