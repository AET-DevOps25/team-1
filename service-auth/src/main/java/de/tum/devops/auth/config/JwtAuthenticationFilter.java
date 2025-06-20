package de.tum.devops.auth.config;

import de.tum.devops.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId; // Changed from userEmail to userId to match token subject

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // Use parseToken for validation and claim extraction.
            // If parseToken doesn't throw an exception, the token is considered valid.
            Claims claims = jwtService.parseToken(jwt);
            userId = claims.getSubject(); // Assuming subject is userId (UUID as String)

            // If token is valid, and there's no authentication in the context
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Extract role from claims
                String roleClaim = claims.get("role", String.class);
                List<GrantedAuthority> authorities;
                if (roleClaim != null && !roleClaim.isBlank()) {
                    // Spring Security expects roles to be prefixed with "ROLE_"
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleClaim.toUpperCase()));
                } else {
                    // Default authority if role claim is missing or empty
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                    logger.warn("Role claim missing or empty for user ID {}, assigning default ROLE_USER.", userId);
                }

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,    // Principal is now userId (String from token's subject)
                        null,      // Credentials
                        authorities // Authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("JWT authentication successful for user ID: {}, authorities: {}", userId, authorities);
            }
        } catch (IllegalArgumentException e) {
            // This catches exceptions from jwtService.parseToken (e.g., expired, malformed, invalid signature)
            logger.warn("JWT validation/processing error for request {}: {}. Token: {}", request.getRequestURI(), e.getMessage(), jwt);
        } catch (Exception e) {
            // Catch any other unexpected exceptions during filter processing
            logger.error("Unexpected error in JwtAuthenticationFilter for request {}: {}. Token: {}", request.getRequestURI(), e.getMessage(), jwt, e);
        }

        filterChain.doFilter(request, response);
    }
}