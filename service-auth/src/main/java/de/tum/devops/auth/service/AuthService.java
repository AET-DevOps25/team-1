package de.tum.devops.auth.service;

import de.tum.devops.auth.dto.AuthResponse;
import de.tum.devops.auth.dto.UserDto;
import de.tum.devops.auth.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Authentication service for handling login, registration, and token operations
 */
@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;

    public AuthService(UserService userService, JwtService jwtService, JwtDecoder jwtDecoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Authenticate user with email and password
     */
    public AuthResponse login(String email, String password) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!userService.verifyPassword(user, password)) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return generateAuthResponse(user);
    }

    /**
     * Register new candidate user
     */
    public AuthResponse register(String fullName, String email, String password) {
        User user = userService.createCandidate(fullName, email, password);
        return generateAuthResponse(user);
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refresh(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);

            // Validate token type
            String tokenType = jwt.getClaim("type");
            if (!"refresh".equals(tokenType)) {
                throw new IllegalArgumentException("Invalid token type");
            }

            // Get user from token
            String userIdStr = jwt.getSubject();
            UUID userId = UUID.fromString(userIdStr);

            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            return generateAuthResponse(user);

        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    /**
     * Get user profile from access token
     */
    public UserDto getProfile(String accessToken) {
        try {
            Jwt jwt = jwtDecoder.decode(accessToken);

            // Validate token type
            String tokenType = jwt.getClaim("type");
            if (!"access".equals(tokenType)) {
                throw new IllegalArgumentException("Invalid token type");
            }

            String userIdStr = jwt.getSubject();
            UUID userId = UUID.fromString(userIdStr);

            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            return userService.convertToDto(user);

        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid access token");
        }
    }

    /**
     * Create HR user (only by existing HR users)
     */
    public UserDto createHRUser(String fullName, String email, String password, String requestorToken) {
        // Verify requestor is HR
        UserDto requestor = getProfile(requestorToken);
        if (!requestor.getRole().name().equals("HR")) {
            throw new IllegalArgumentException("Only HR users can create other HR users");
        }

        User newHRUser = userService.createHRUser(fullName, email, password);
        return userService.convertToDto(newHRUser);
    }

    /**
     * Generate authentication response with tokens
     */
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        UserDto userDto = userService.convertToDto(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                userDto,
                jwtService.getAccessTokenExpiration());
    }

    /**
     * Extract user ID from JWT token
     */
    public UUID extractUserIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return UUID.fromString(jwt.getSubject());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    /**
     * Validate token and return user info
     */
    public UserDto validateTokenAndGetUser(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            UUID userId = UUID.fromString(jwt.getSubject());

            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            return userService.convertToDto(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}