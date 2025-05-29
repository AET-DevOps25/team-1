package de.tum.devops.persistence.entity;

/**
 * Chat status enumeration matching database chat_status enum
 * 
 * Database enum definition:
 * CREATE TYPE chat_status AS ENUM ('ACTIVE', 'COMPLETED', 'EXPIRED');
 */
public enum ChatStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED
}