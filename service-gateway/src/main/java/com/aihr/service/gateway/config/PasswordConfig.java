package com.aihr.service.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password encoding using Argon2id
 */
@Configuration
public class PasswordConfig {

    /**
     * Configure Argon2id password encoder with secure parameters
     * Memory: 64MB (65536 KB)
     * Iterations: 10
     * Parallelism: 2
     * Hash length: 32 bytes
     * 
     * @return configured Argon2PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * Alternative configuration with custom parameters
     * Uncomment if you need to customize Argon2 parameters
     */
    /*
     * @Bean
     * public PasswordEncoder customPasswordEncoder() {
     * return new Argon2PasswordEncoder(
     * 16, // saltLength
     * 32, // hashLength
     * 2, // parallelism
     * 65536, // memory (64MB)
     * 10 // iterations
     * );
     * }
     */
}