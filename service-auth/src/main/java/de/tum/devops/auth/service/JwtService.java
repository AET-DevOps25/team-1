package de.tum.devops.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Service for JWT token operations
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.private-key}")
    private String privateKeyString;

    @Value("${app.jwt.public-key}")
    private String publicKeyString;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Initialize RSA keys from environment variables
     */
    private void initializeKeys() {
        if (privateKey == null || publicKey == null) {
            try {
                // Parse private key
                privateKey = parsePrivateKey(privateKeyString);

                // Parse public key
                publicKey = parsePublicKey(publicKeyString);

                logger.info("JWT RSA keys loaded successfully from environment variables");
            } catch (Exception e) {
                logger.error("Failed to load RSA keys from environment variables, generating fallback keys: {}",
                        e.getMessage());
                throw new IllegalStateException("Missing or invalid RSA keys for JWT. Shutting down.", e);
            }
        }
    }

    /**
     * Parse RSA private key from string
     */
    private PrivateKey parsePrivateKey(String keyString) throws Exception {
        String privateKeyPEM = keyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * Parse RSA public key from string
     */
    private PublicKey parsePublicKey(String keyString) throws Exception {
        String publicKeyPEM = keyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * Generate access token
     */
    public String generateAccessToken(String userEmail, UUID userId, String role, String fullName) {
        initializeKeys();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", userEmail)
                .claim("role", role)
                .claim("fullName", fullName)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }


    /**
     * Parse and validate JWT token
     */
    public Claims parseToken(String token) {
        initializeKeys();

        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw new IllegalArgumentException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported: {}", e.getMessage());
            throw new IllegalArgumentException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            logger.warn("JWT token is malformed: {}", e.getMessage());
            throw new IllegalArgumentException("JWT token is malformed", e);
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            throw new IllegalArgumentException("JWT token compact of handler are invalid", e);
        }
    }

    /**
     * Extract user ID from token
     */
    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extract user email from token
     */
    public String extractEmail(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract user role from token
     */
    public String extractRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get public key for validation by other services
     */
    public PublicKey getPublicKey() {
        initializeKeys();
        return publicKey;
    }
}
