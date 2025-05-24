package com.aihr.service.gateway.service;

import com.aihr.service.gateway.dto.LoginRequestDto;
import com.aihr.service.gateway.dto.RegisterRequestDto;
import com.aihr.service.gateway.dto.UserDto;
import com.aihr.service.gateway.entity.User;
import com.aihr.service.gateway.entity.UserRole;
import com.aihr.service.gateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for user-related operations
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     */
    public UserDto registerUser(RegisterRequestDto registerRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("User with email " + registerRequest.getEmail() + " already exists");
        }

        // Create new user
        User user = new User(
                registerRequest.getFullName(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getRole());

        // Save user
        User savedUser = userRepository.save(user);
        return UserDto.fromUser(savedUser);
    }

    /**
     * Authenticate user with email and password
     */
    public Optional<User> authenticateUser(LoginRequestDto loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    /**
     * Find user by email
     */
    public Optional<UserDto> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::fromUser);
    }

    /**
     * Find user by ID
     */
    public Optional<UserDto> findUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(UserDto::fromUser);
    }

    /**
     * Get user entity by ID (for internal use)
     */
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Get user entity by email (for internal use)
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all HR users
     */
    public List<UserDto> getAllHRUsers() {
        return userRepository.findByRole(UserRole.HR)
                .stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Get all candidate users
     */
    public List<UserDto> getAllCandidates() {
        return userRepository.findByRole(UserRole.CANDIDATE)
                .stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Check if any HR users exist
     */
    public boolean hasAnyHRUsers() {
        return userRepository.existsAnyHRUser();
    }

    /**
     * Get user count by role
     */
    public long getUserCountByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * Add HR user (only accessible by existing HR users)
     */
    public UserDto addHRUser(RegisterRequestDto registerRequest) {
        // Force role to HR
        registerRequest.setRole(UserRole.HR);
        return registerUser(registerRequest);
    }

    /**
     * Search users by email pattern
     */
    public List<UserDto> searchUsersByEmail(String emailPattern) {
        return userRepository.findByEmailContaining(emailPattern)
                .stream()
                .map(UserDto::fromUser)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile (basic info only)
     */
    public UserDto updateUserProfile(UUID userId, String fullName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(fullName);
        User updatedUser = userRepository.save(user);
        return UserDto.fromUser(updatedUser);
    }

    /**
     * Change user password
     */
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}