package de.tum.devops.application.persistence.repository;

import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.enums.MessageSender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ChatMessage entity
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Find messages by session ID ordered by timestamp
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(UUID sessionId);

    /**
     * Find messages by session ID with pagination
     */
    Page<ChatMessage> findBySessionId(UUID sessionId, Pageable pageable);

    /**
     * Find messages by session ID and sender
     */
    List<ChatMessage> findBySessionIdAndSender(UUID sessionId, MessageSender sender);

    /**
     * Count messages by session ID
     */
    long countBySessionId(UUID sessionId);

    /**
     * Count messages by session ID and sender
     */
    long countBySessionIdAndSender(UUID sessionId, MessageSender sender);
}