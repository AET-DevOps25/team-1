package de.tum.devops.application.entity;

/**
 * Application status enumeration matching database application_status enum
 */
public enum ApplicationStatus {
    PENDING,
    UNDER_REVIEW,
    REJECTED,
    ACCEPTED
}