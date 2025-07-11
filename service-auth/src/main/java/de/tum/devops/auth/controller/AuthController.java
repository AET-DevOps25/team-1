package de.tum.devops.auth.controller;

import de.tum.devops.auth.dto.*;
import de.tum.devops.auth.service.AuthService;
import de.tum.devops.auth.service.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    @Value("${app.cookie.domain}")
    private String cookieDomain;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * POST /api/v1/auth/login
     * User login using email and password
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword());

        logger.info("Login successful for user: {}", authResponse.getUser().getUserID());

        // create secure cookie with JWT
        ResponseCookie cookie = ResponseCookie.from("auth_token", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(cookieDomain)
                .maxAge(authResponse.getExpiresIn())
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * POST /api/v1/auth/register
     * New candidate account registration (defaults to CANDIDATE role)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        AuthResponse authResponse = authService.register(
                request.getFullName(),
                request.getEmail(),
                request.getPassword());

        logger.info("Registration successful for user: {}", authResponse.getUser().getUserID());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(authResponse));
    }

    /**
     * POST /api/v1/auth/hr-register
     * Existing HR users create other HR accounts
     */
    @PreAuthorize("hasRole('HR')")
    @PostMapping("/hr-register")
    public ResponseEntity<ApiResponse<UserDto>> hrRegister(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal String requestorId) {

        logger.info("HR register attempt for email: {} by HR user: {}", request.getEmail(), requestorId);

        UserDto newHR = authService.createHRUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                requestorId);

        logger.info("HR user created: {} by HR user: {}", newHR.getUserID(), requestorId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(newHR));
    }

    /**
     * GET /api/v1/auth/index.html
     * Simple login page (HTML form)
     */
    @GetMapping(value = "/index.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> loginPage() {
        Resource resource = new ClassPathResource("static/api/v1/auth/index.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
    }

    /**
     * GET /api/v1/auth/verify
     * Verify JWT for HR role; returns 200 when token has ROLE_HR
     */
    @GetMapping("/verify")
    public ResponseEntity<Void> verifyJwt(@CookieValue(name = "auth_token", required = false) String tokenCookie,
                                          @RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = tokenCookie;
        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String role = jwtService.extractRole(token);
            if ("HR".equals(role)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}