package de.tum.devops.application.persistence.enums;

/**
 * Recommendation enumeration matching database recommendation_enum
 * 
 * Database enum definition:
 * CREATE TYPE recommendation_enum AS ENUM ('RECOMMEND', 'CONSIDER',
 * 'NOT_RECOMMEND');
 */
public enum RecommendationEnum {
    RECOMMEND,
    CONSIDER,
    NOT_RECOMMEND
}