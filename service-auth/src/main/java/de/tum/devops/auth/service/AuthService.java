package de.tum.devops.auth.service;

import de.tum.devops.auth.dto.AuthResponse;
import de.tum.devops.auth.dto.UserDto;
import de.tum.devops.auth.persistence.entity.User;
import de.tum.devops.auth.persistence.enums.UserRole;
import de.tum.devops.auth.persistence.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication service for handling login, registration, and token operations
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Convert User entity to UserDto
     */
    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreationTimestamp());
    }

    /**
     * Verify password
     */
    private boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * Authenticate user with email and password
     */
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!verifyPassword(user, password)) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return generateAuthResponse(user);
    }

    /**
     * Register new candidate user
     */
    public AuthResponse register(String fullName, String email, String password) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.CANDIDATE);

        user = userRepository.save(user);
        return generateAuthResponse(user);
    }

    /**
     * Internal fetch user by ID (no auth)
     */
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return convertToDto(user);
    }

    /**
     * Create HR user (only by existing HR users)
     */
    public UserDto createHRUser(String fullName, String email, String password, String requestorToken) {
        // Verify requestor is HR
        UserDto requestor = getUserById(extractUserIdFromToken(requestorToken));
        if (!requestor.getRole().name().equals("HR")) {
            throw new IllegalArgumentException("Only HR users can create other HR users");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newHRUser = new User();
        newHRUser.setFullName(fullName);
        newHRUser.setEmail(email);
        newHRUser.setPasswordHash(passwordEncoder.encode(password));
        newHRUser.setRole(UserRole.HR);

        newHRUser = userRepository.save(newHRUser);
        return convertToDto(newHRUser);
    }

    /**
     * Generate authentication response with token
     */
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getUserId(),
                user.getRole().name(),
                user.getFullName());
        UserDto userDto = convertToDto(user);

        return new AuthResponse(
                accessToken,
                userDto,
                3600); // 1 hour in seconds
    }

    /**
     * Extract user ID from JWT token
     */
    public UUID extractUserIdFromToken(String token) {
        try {
            return jwtService.extractUserId(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}
