package de.tum.devops.auth.controller;

import de.tum.devops.auth.dto.ApiResponse;
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
 * User Management Controller
 * Implements the HR user management endpoint from api-documentation.yaml:
 * - POST /api/v1/users/hr
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/users/hr
     * Existing HR users create HR accounts for others
     * Requires HR role authorization
     */
    @PostMapping("/hr")
    public ResponseEntity<ApiResponse<UserDto>> createHRUser(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("HR user creation attempt for email: {}", request.getEmail());

        // Extract token from Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header is required");
        }

        String token = authHeader.substring(7);

        UserDto newHRUser = authService.createHRUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                token);

        logger.info("HR user created successfully: {}", newHRUser.getUserID());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(newHRUser));
    }
}