package com.aihr.service.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JWT properties
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400}") // Default: 24 hours
    private long expiration;

    @Value("${jwt.refresh-expiration:604800}") // Default: 7 days
    private long refreshExpiration;

    @Value("${jwt.issuer:ai-hr-system}")
    private String issuer;

    // Getters
    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    // Utility methods
    public long getExpirationMs() {
        return expiration * 1000;
    }

    public long getRefreshExpirationMs() {
        return refreshExpiration * 1000;
    }
}