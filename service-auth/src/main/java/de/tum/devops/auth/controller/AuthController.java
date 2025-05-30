package de.tum.devops.auth.controller;

import de.tum.devops.auth.dto.ApiResponse;
import de.tum.devops.auth.dto.AuthResponse;
import de.tum.devops.auth.dto.LoginRequest;
import de.tum.devops.auth.dto.RegisterRequest;
import de.tum.devops.auth.dto.UserDto;
import de.tum.devops.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Implements the 4 authentication endpoints from api-documentation.yaml:
 * - POST /api/v1/auth/login
 * - POST /api/v1/auth/register
 * - POST /api/v1/auth/logout
 * - GET /api/v1/auth/profile
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/login
     * User login using email and password
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword());

        logger.info("Login successful for user: {}", authResponse.getUser().getUserID());

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * POST /api/v1/auth/register
     * New candidate account registration (defaults to CANDIDATE role)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        AuthResponse authResponse = authService.register(
                request.getFullName(),
                request.getEmail(),
                request.getPassword());

        logger.info("Registration successful for user: {}", authResponse.getUser().getUserID());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(authResponse));
    }


    /**
     * POST /api/v1/auth/logout
     * User logout (simple response, no token revocation in this implementation)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestHeader("Authorization") String authHeader) {
        logger.info("Logout request received");

        // Extract token from Authorization header (format: "Bearer <token>")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Validate token and get user info for logging
            try {
                UserDto user = authService.validateTokenAndGetUser(token);
                logger.info("Logout successful for user: {}", user.getUserID());
            } catch (Exception e) {
                logger.warn("Logout with invalid token");
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * GET /api/v1/auth/profile
     * Get detailed information of the current logged-in user
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(@RequestHeader("Authorization") String authHeader) {
        logger.info("Profile request received");

        // Extract token from Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is required");
        }

        String token = authHeader.substring(7);
        UserDto user = authService.getProfile(token);

        logger.info("Profile retrieved for user: {}", user.getUserID());

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", user));
    }
}
