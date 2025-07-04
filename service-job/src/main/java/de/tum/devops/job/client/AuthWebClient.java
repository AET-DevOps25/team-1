package de.tum.devops.job.client;

import de.tum.devops.job.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Simple wrapper around Spring WebClient for calling service-auth internal endpoints.
 */
@Component
public class AuthWebClient {

    private static final Logger log = LoggerFactory.getLogger(AuthWebClient.class);

    private final WebClient webClient;

    public AuthWebClient(WebClient.Builder builder,
                         @Value("${service.auth.base-url:http://service-auth}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<UserDto> fetchUser(UUID id) {
        return webClient.get()
                .uri("/internal/api/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDto>>() {})
                .flatMap(resp -> {
                    if (resp == null || !Boolean.TRUE.equals(resp.success())) {
                        log.warn("Failed to fetch user {} from auth service", id);
                        return Mono.empty();
                    }
                    return Mono.justOrEmpty(resp.data());
                })
                .onErrorResume(ex -> {
                    log.error("Error calling auth service", ex);
                    return Mono.empty();
                });
    }

    /**
     * Generic ApiResponse record matching service-auth response structure.
     */
    public static record ApiResponse<T>(Boolean success, String message, T data, Integer code) {
    }
}
