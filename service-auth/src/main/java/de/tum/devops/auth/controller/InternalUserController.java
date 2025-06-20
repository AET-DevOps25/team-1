package de.tum.devops.auth.controller;

import de.tum.devops.auth.dto.ApiResponse;
import de.tum.devops.auth.dto.UserDto;
import de.tum.devops.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal user info endpoint (cluster-internal, no auth).
 * GET /internal/api/v1/users/{id}
 */
@RestController
@RequestMapping("/internal/api/v1/users")
public class InternalUserController {

    private static final Logger logger = LoggerFactory.getLogger(InternalUserController.class);

    private final AuthService authService;

    public InternalUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable("id") UUID id) {
        logger.info("Internal fetch user {}", id);
        UserDto dto = authService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved", dto));
    }
}
