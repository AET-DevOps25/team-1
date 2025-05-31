package de.tum.devops.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Persistence configuration for entity scanning and repository enabling
 */
@Configuration
@EntityScan(basePackages = "de.tum.devops.persistence.entity")
@EnableJpaRepositories(basePackages = "de.tum.devops.persistence.repository")
public class PersistenceConfig {
}