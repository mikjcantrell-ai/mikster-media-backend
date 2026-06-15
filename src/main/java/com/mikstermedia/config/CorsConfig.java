package com.mikstermedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration for the AI Music Web API.
 *
 * <p>The Angular development server runs at {@code http://localhost:4200}.
 * This config allows that origin to reach every endpoint under {@code /api/**}
 * with the standard HTTP verbs used by the frontend services.
 *
 * <p>In production, replace the allowed origin with your deployed frontend URL
 * and tighten the allowed headers list as needed.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        // Support all standard REST verbs + preflight
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        // Allow common headers sent by Angular's HttpClient
                        .allowedHeaders("Content-Type", "Authorization", "Accept",
                                        "X-Requested-With", "Cache-Control")
                        // Expose no custom response headers to the browser
                        .exposedHeaders()
                        // Allow cookies / credentials if ever needed
                        .allowCredentials(true)
                        // Cache preflight for 1 hour to reduce OPTIONS calls
                        .maxAge(3600);
            }
        };
    }
}
