package de.tum.devops.job.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.public-key}")
    private String publicKeyString;

    private PublicKey publicKey;

    private void initKey() {
        if (publicKey != null) return;
        try {
            String pem = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            logger.error("Failed to parse RSA public key", e);
            throw new IllegalStateException("Invalid RSA public key", e);
        }
    }

    public Claims parseToken(String token) {
        initKey();
        try {
            return Jwts.parser().verifyWith(publicKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (SecurityException e) {
            logger.warn("Invalid signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token signature", e);
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired");
            throw new IllegalArgumentException("Token expired", e);
        } catch (JwtException e) {
            logger.warn("Token parsing error: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
