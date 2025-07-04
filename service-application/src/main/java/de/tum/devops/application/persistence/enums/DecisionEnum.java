package de.tum.devops.application.persistence.enums;

/**
 * HR Decision enumeration matching database decision_enum
 * 
 * Database enum definition:
 * CREATE TYPE decision_enum AS ENUM ('SHORTLISTED', 'REJECTED', 'HIRED');
 */
public enum DecisionEnum {
    SHORTLISTED,
    REJECTED,
    HIRED
}