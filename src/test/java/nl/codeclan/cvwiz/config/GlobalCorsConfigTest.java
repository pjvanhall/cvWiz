package nl.codeclan.cvwiz.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalCorsConfigTest {

    @Test
    void registersConfiguredCorsMapping() {
        ExposedCorsRegistry registry = new ExposedCorsRegistry();

        new GlobalCorsConfig().corsConfigurer().addCorsMappings(registry);

        CorsConfiguration configuration = registry.configurations().get("/**");
        assertThat(configuration.getAllowedOrigins()).contains(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:4200",
                "http://127.0.0.1:5173"
        );
        assertThat(configuration.getAllowedMethods()).containsExactly("POST", "GET", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).containsExactly("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With");
        assertThat(configuration.getAllowCredentials()).isFalse();
        assertThat(configuration.getMaxAge()).isEqualTo(3600);
    }

    private static final class ExposedCorsRegistry extends CorsRegistry {
        private Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
