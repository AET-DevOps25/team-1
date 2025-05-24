package com.aihr.service.gateway.controller;

import com.aihr.service.gateway.dto.RegisterRequestDto;
import com.aihr.service.gateway.dto.UserDto;
import com.aihr.service.gateway.entity.UserRole;
import com.aihr.service.gateway.service.AuthService;
import com.aihr.service.gateway.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for user management operations
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Add new HR user (only accessible by existing HR users)
     */
    @PostMapping("/hr")
    public ResponseEntity<?> addHRUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RegisterRequestDto registerRequest) {
        try {
            // Validate token and check if user is HR
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty() || !UserRole.HR.equals(currentUser.get().getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only HR users can add new HR users"));
            }

            // Add HR user
            UserDto newHRUser = userService.addHRUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newHRUser);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add HR user: " + e.getMessage()));
        }
    }

    /**
     * Get all HR users (only accessible by HR users)
     */
    @GetMapping("/hr")
    public ResponseEntity<?> getAllHRUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and check if user is HR
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty() || !UserRole.HR.equals(currentUser.get().getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only HR users can view HR user list"));
            }

            List<UserDto> hrUsers = userService.getAllHRUsers();
            return ResponseEntity.ok(hrUsers);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get HR users: " + e.getMessage()));
        }
    }

    /**
     * Get all candidates (only accessible by HR users)
     */
    @GetMapping("/candidates")
    public ResponseEntity<?> getAllCandidates(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validate token and check if user is HR
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty() || !UserRole.HR.equals(currentUser.get().getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only HR users can view candidate list"));
            }

            List<UserDto> candidates = userService.getAllCandidates();
            return ResponseEntity.ok(candidates);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get candidates: " + e.getMessage()));
        }
    }

    /**
     * Search users by email pattern (only accessible by HR users)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String email) {
        try {
            // Validate token and check if user is HR
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty() || !UserRole.HR.equals(currentUser.get().getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only HR users can search users"));
            }

            List<UserDto> users = userService.searchUsersByEmail(email);
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search users: " + e.getMessage()));
        }
    }

    /**
     * Get user by ID (accessible by the user themselves or HR users)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId) {
        try {
            // Validate token
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Check if user is accessing their own profile or is HR
            boolean isOwnProfile = currentUser.get().getUserID().equals(userId);
            boolean isHR = UserRole.HR.equals(currentUser.get().getRole());

            if (!isOwnProfile && !isHR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Optional<UserDto> user = userService.findUserById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get user: " + e.getMessage()));
        }
    }

    /**
     * Update user profile (users can only update their own profile)
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @RequestBody Map<String, String> updateRequest) {
        try {
            // Validate token
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Check if user is updating their own profile
            if (!currentUser.get().getUserID().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only update your own profile"));
            }

            String fullName = updateRequest.get("fullName");
            if (fullName == null || fullName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Full name is required"));
            }

            UserDto updatedUser = userService.updateUserProfile(userId, fullName);
            return ResponseEntity.ok(updatedUser);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Change user password (users can only change their own password)
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @RequestBody Map<String, String> passwordRequest) {
        try {
            // Validate token
            String token = extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            Optional<UserDto> currentUser = authService.validateToken(token);
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Check if user is changing their own password
            if (!currentUser.get().getUserID().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only change your own password"));
            }

            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Current password and new password are required"));
            }

            userService.changePassword(userId, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to change password: " + e.getMessage()));
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}