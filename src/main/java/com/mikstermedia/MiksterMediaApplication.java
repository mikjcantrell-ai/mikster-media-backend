package com.mikstermedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the AI Music Web Spring Boot backend.
 *
 * The application exposes a REST API consumed by the Angular SPA running at
 * http://localhost:4200.  All cross-origin policy is configured in
 * {@link com.mikstermedia.config.CorsConfig}.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MiksterMediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiksterMediaApplication.class, args);
    }
}
