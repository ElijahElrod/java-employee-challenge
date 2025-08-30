package com.reliaquest.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for external employee API client settings.
 * <p>
 * Binds properties defined with the prefix {@code api.employee} from the application
 * configuration (e.g., {@code application.yml} or {@code application.properties})
 * into this class.
 * </p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * api:
 *   employee:
 *     base-url: https://example.com/api/employees
 * </pre>
 *
 * <ul>
 *   <li>{@link #baseUrl} – The base URL of the employee API (required).</li>
 * </ul>
 *
 * <p>
 * The {@link NotBlank} constraint ensures that the property must be provided
 * at runtime; otherwise, the application will fail on startup.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api.employee")
public class ClientConfig {

    @NotBlank
    private String baseUrl;
}
