package de.tum.devops.application.client;

import de.tum.devops.application.dto.JobDto;
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
 * Simple wrapper around Spring WebClient for calling service-job internal endpoints.
 */
@Component
public class JobWebClient {

    private static final Logger log = LoggerFactory.getLogger(JobWebClient.class);

    private final WebClient webClient;

    public JobWebClient(WebClient.Builder builder,
                        @Value("${service.job.base-url:http://job-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<JobDto> fetchJob(UUID id) {
        return webClient.get()
                .uri("/internal/api/v1/jobs/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<JobDto>>() {
                })
                .flatMap(resp -> {
                    if (resp == null || !Boolean.TRUE.equals(resp.success())) {
                        log.warn("Failed to fetch job {} from job service", id);
                        return Mono.empty();
                    }
                    return Mono.justOrEmpty(resp.data());
                })
                .onErrorResume(ex -> {
                    log.error("Error calling job service", ex);
                    return Mono.empty();
                });
    }

    /**
     * Generic ApiResponse record matching service-job response structure.
     * This can be refactored into a common library later.
     */
    public static record ApiResponse<T>(Boolean success, String message, T data, Integer code) {
    }
}
