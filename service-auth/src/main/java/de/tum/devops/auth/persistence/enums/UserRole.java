package de.tum.devops.auth.persistence.enums;

/**
 * User role enum corresponding to 'user_role' type in init.sql
 * 
 * SQL definition:
 * CREATE TYPE user_role AS ENUM ('CANDIDATE', 'HR');
 */
public enum UserRole {
    CANDIDATE,
    HR
}