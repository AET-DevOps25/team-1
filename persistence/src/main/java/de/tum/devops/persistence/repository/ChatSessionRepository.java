package de.tum.devops.persistence.repository;

import de.tum.devops.persistence.entity.ChatSession;
import de.tum.devops.persistence.enums.ChatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ChatSession entity
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    /**
     * Find chat session by application ID
     */
    Optional<ChatSession> findByApplicationId(UUID applicationId);

    /**
     * Find chat sessions by status
     */
    Page<ChatSession> findByStatus(ChatStatus status, Pageable pageable);

    /**
     * Check if chat session exists for application
     */
    boolean existsByApplicationId(UUID applicationId);

    /**
     * Count chat sessions by status
     */
    long countByStatus(ChatStatus status);
}