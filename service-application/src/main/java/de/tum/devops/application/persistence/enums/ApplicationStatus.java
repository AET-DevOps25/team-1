package de.tum.devops.application.persistence.enums;

/**
 * Application status enumeration matching database application_status enum
 * 
 * Database enum definition:
 * CREATE TYPE application_status AS ENUM (
 * 'SUBMITTED',
 * 'AI_SCREENING',
 * 'AI_INTERVIEW',
 * 'COMPLETED',
 * 'SHORTLISTED',
 * 'REJECTED',
 * 'HIRED'
 * );
 */
public enum ApplicationStatus {
    SUBMITTED,
    AI_SCREENING,
    AI_INTERVIEW,
    COMPLETED,
    SHORTLISTED,
    REJECTED,
    HIRED
}