package com.aihr.service.gateway.service;

import com.aihr.service.gateway.dto.AuthResponseDto;
import com.aihr.service.gateway.dto.LoginRequestDto;
import com.aihr.service.gateway.dto.RegisterRequestDto;
import com.aihr.service.gateway.dto.UserDto;
import com.aihr.service.gateway.entity.User;
import com.aihr.service.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for authentication operations
 */
@Service
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate user and generate tokens
     */
    public Optional<AuthResponseDto> login(LoginRequestDto loginRequest) {
        Optional<User> userOpt = userService.authenticateUser(loginRequest);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // Create response
            UserDto userDto = UserDto.fromUser(user);
            AuthResponseDto response = new AuthResponseDto(
                    accessToken,
                    refreshToken,
                    86400, // 24 hours in seconds
                    userDto);

            return Optional.of(response);
        }

        return Optional.empty();
    }

    /**
     * Register new user and generate tokens
     */
    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        // Register user
        UserDto userDto = userService.registerUser(registerRequest);

        // Get user entity for token generation
        User user = userService.getUserByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User registration failed"));

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Create response
        return new AuthResponseDto(
                accessToken,
                refreshToken,
                86400, // 24 hours in seconds
                userDto);
    }

    /**
     * Refresh access token using refresh token
     */
    public Optional<AuthResponseDto> refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            if (!jwtUtil.isValidToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                return Optional.empty();
            }

            // Extract user from token
            String email = jwtUtil.extractEmail(refreshToken);
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Generate new tokens
                String newAccessToken = jwtUtil.generateAccessToken(user);
                String newRefreshToken = jwtUtil.generateRefreshToken(user);

                // Create response
                UserDto userDto = UserDto.fromUser(user);
                AuthResponseDto response = new AuthResponseDto(
                        newAccessToken,
                        newRefreshToken,
                        86400, // 24 hours in seconds
                        userDto);

                return Optional.of(response);
            }
        } catch (Exception e) {
            // Token validation failed
            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Validate access token and get user info
     */
    public Optional<UserDto> validateToken(String accessToken) {
        try {
            if (jwtUtil.isValidToken(accessToken) && !jwtUtil.isRefreshToken(accessToken)) {
                String email = jwtUtil.extractEmail(accessToken);
                return userService.findUserByEmail(email);
            }
        } catch (Exception e) {
            // Token validation failed
        }

        return Optional.empty();
    }

    /**
     * Get user profile from token
     */
    public Optional<UserDto> getUserProfile(String accessToken) {
        return validateToken(accessToken);
    }

    /**
     * Logout user (in a real implementation, you might want to blacklist the token)
     */
    public void logout(String accessToken) {
        // In a simple implementation, logout is handled client-side by discarding
        // tokens
        // In a more sophisticated implementation, you would:
        // 1. Add token to blacklist
        // 2. Store blacklisted tokens in Redis/Database
        // 3. Check blacklist during token validation

        // For now, we'll just validate the token to ensure it's legitimate
        validateToken(accessToken);
    }
}