package com.aihr.service.gateway.entity;

/**
 * User roles in the AI-HR system
 */
@jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
public enum UserRole {
    /**
     * Candidate user role - for job applicants
     */
    CANDIDATE,

    /**
     * HR user role - for HR personnel
     */
    HR
}