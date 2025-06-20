package de.tum.devops.auth.persistence.repository;

import de.tum.devops.auth.persistence.entity.User;
import de.tum.devops.auth.persistence.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User repository for database operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (for login)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists (for registration validation)
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role (for admin purposes)
     */
    long countByRole(UserRole role);
}