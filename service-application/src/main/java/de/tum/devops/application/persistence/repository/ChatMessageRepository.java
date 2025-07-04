package de.tum.devops.application.persistence.repository;

import de.tum.devops.application.persistence.entity.ChatMessage;
import de.tum.devops.application.persistence.entity.ChatSession;
import de.tum.devops.application.persistence.enums.MessageSender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ChatMessage entity
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Find messages by session ID with pagination, ordered by sent time
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.sessionId = :sessionId ORDER BY m.sentAt ASC")
    Page<ChatMessage> findBySessionIdOrderBySentAtAsc(UUID sessionId, Pageable pageable);

    /**
     * Find messages by session ID, ordered by sent time (for full conversation)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.sessionId = :sessionId ORDER BY m.sentAt ASC")
    List<ChatMessage> findBySessionIdOrderBySentAtAsc(UUID sessionId);

    /**
     * Find messages by session with pagination
     */
    Page<ChatMessage> findBySessionOrderBySentAtAsc(ChatSession session, Pageable pageable);

    /**
     * Find messages by session and sender
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.sessionId = :sessionId AND m.sender = :sender ORDER BY m.sentAt ASC")
    List<ChatMessage> findBySessionIdAndSenderOrderBySentAtAsc(UUID sessionId, MessageSender sender);

    /**
     * Count messages by sender in a session
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.session.sessionId = :sessionId AND m.sender = :sender")
    long countBySessionIdAndSender(UUID sessionId, MessageSender sender);

    /**
     * Find latest message in a session
     */
    ChatMessage findFirstBySessionSessionIdOrderBySentAtDesc(UUID sessionId);

    /**
     * Find messages by application ID (through session relationship)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.application.applicationId = :applicationId ORDER BY m.sentAt ASC")
    List<ChatMessage> findByApplicationIdOrderBySentAtAsc(@Param("applicationId") UUID applicationId);

    /**
     * Find messages by application ID with pagination
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.application.applicationId = :applicationId ORDER BY m.sentAt ASC")
    Page<ChatMessage> findByApplicationIdOrderBySentAtAsc(@Param("applicationId") UUID applicationId, Pageable pageable);
}