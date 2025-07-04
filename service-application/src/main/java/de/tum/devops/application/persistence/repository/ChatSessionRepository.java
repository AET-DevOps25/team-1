package de.tum.devops.application.persistence.repository;

import de.tum.devops.application.persistence.entity.Application;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ChatSession entity
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    
    /**
     * Find chat session by application
     */
    Optional<ChatSession> findByApplication(Application application);
    
    /**
     * Find chat session by application ID
     */
    Optional<ChatSession> findByApplicationApplicationId(UUID applicationId);
    
    /**
     * Find chat sessions by status
     */
    List<ChatSession> findByStatus(ChatStatus status);
    
    /**
     * Find active chat sessions
     */
    List<ChatSession> findByStatusOrderByStartedAtDesc(ChatStatus status);
    
    /**
     * Check if application has a chat session
     */
    boolean existsByApplicationApplicationId(UUID applicationId);
    
    /**
     * Find chat session with messages loaded
     */
    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.messages WHERE cs.sessionId = :sessionId")
    Optional<ChatSession> findByIdWithMessages(@Param("sessionId") UUID sessionId);
    
    /**
     * Find chat session by application with messages loaded
     */
    @Query("SELECT cs FROM ChatSession cs LEFT JOIN FETCH cs.messages WHERE cs.application.applicationId = :applicationId")
    Optional<ChatSession> findByApplicationIdWithMessages(@Param("applicationId") UUID applicationId);
    
    /**
     * Count sessions by status
     */
    long countByStatus(ChatStatus status);
}