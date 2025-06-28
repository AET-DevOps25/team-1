package de.tum.devops.application.persistence.enums;

/**
 * Application status enumeration matching database application_status enum
 * <p>
 * Database enum definition:
 * CREATE TYPE application_status AS ENUM (
 * 'SUBMITTED',
 * 'AI_SCREENING',
 * 'AI_INTERVIEW',
 * 'COMPLETED',
 * );
 */
public enum ApplicationStatus {
    SUBMITTED,
    AI_SCREENING,
    AI_INTERVIEW,
    COMPLETED,
}