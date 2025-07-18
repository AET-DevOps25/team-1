package de.tum.devops.auth.service;

import de.tum.devops.auth.dto.UserDto;
import de.tum.devops.auth.persistence.entity.User;
import de.tum.devops.auth.persistence.enums.UserRole;
import de.tum.devops.auth.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Lightweight tests for {@link UserService} focusing on pure Java logic that does not
 * require database connections.
 */
class UserServiceUTest {

    private UserService userService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void convertToDtoCopiesFields() {
        // Arrange
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setFullName("Alice Wonderland");
        user.setEmail("alice@example.com");
        user.setRole(UserRole.CANDIDATE);
        user.setCreatedAt(LocalDateTime.now());

        // Act
        UserDto dto = userService.convertToDto(user);

        // Assert
        assertEquals(user.getUserId(), dto.getUserID());
        assertEquals(user.getFullName(), dto.getFullName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getRole(), dto.getRole());
    }

    @Test
    void verifyPasswordDelegatesToPasswordEncoder() {
        // Arrange
        User user = new User();
        user.setPasswordHash("hashed");
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);

        // Act & Assert
        assertTrue(userService.verifyPassword(user, "plain"));
    }
}