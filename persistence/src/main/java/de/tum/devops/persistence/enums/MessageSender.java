package de.tum.devops.persistence.enums;

/**
 * Message sender enumeration matching database message_sender enum
 * 
 * Database enum definition:
 * CREATE TYPE message_sender AS ENUM ('AI', 'CANDIDATE');
 */
public enum MessageSender {
    AI,
    CANDIDATE
}