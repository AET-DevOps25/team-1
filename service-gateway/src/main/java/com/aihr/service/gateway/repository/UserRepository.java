package com.aihr.service.gateway.repository;

import com.aihr.service.gateway.entity.User;
import com.aihr.service.gateway.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * 
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     * 
     * @param role the user role
     * @return list of users with the specified role
     */
    List<User> findByRole(UserRole role);

    /**
     * Count users by role
     * 
     * @param role the user role
     * @return number of users with the specified role
     */
    long countByRole(UserRole role);

    /**
     * Find the first HR user (for initialization purposes)
     * 
     * @return Optional containing the first HR user if found
     */
    @Query("SELECT u FROM User u WHERE u.role = 'HR' ORDER BY u.creationTimestamp ASC LIMIT 1")
    Optional<User> findFirstHRUser();

    /**
     * Check if any HR users exist
     * 
     * @return true if at least one HR user exists
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role = 'HR'")
    boolean existsAnyHRUser();

    /**
     * Find user by email and role
     * 
     * @param email the email address
     * @param role  the user role
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailAndRole(String email, UserRole role);

    /**
     * Find users created after a specific date
     * 
     * @param email the email pattern for search
     * @return list of users matching the email pattern
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> findByEmailContaining(@Param("email") String email);
}