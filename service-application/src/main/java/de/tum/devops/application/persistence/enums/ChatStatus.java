package de.tum.devops.application.persistence.enums;

/**
 * Chat status enumeration matching database chat_status enum
 * 
 * Database enum definition:
 * CREATE TYPE chat_status AS ENUM ('ACTIVE', 'COMPLETED');
 */
public enum ChatStatus {
    ACTIVE,
    COMPLETE
}