package com.mikstermedia.config;

import com.mikstermedia.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Spring Security configuration — DB-backed authentication.
 *
 * Strategy:
 *   - UserDetailsService loads from the app_users table in aimusic.db
 *   - All GET endpoints → public (no auth required)
 *   - POST / PUT / PATCH / DELETE on /api/** → require ADMIN role
 *   - CSRF disabled (stateless REST API consumed by Angular SPA)
 *   - Stateless sessions — credentials sent per-request via Authorization header
 *
 * The initial admin account is seeded by DataInitializer on first startup
 * using credentials from application.properties (app.admin.*).
 * After that, additional users are managed via /api/users.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: GET and OPTIONS
                .requestMatchers(HttpMethod.GET,     "/api/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS,  "/**").permitAll()
                // Public write operations (community join, contact form)
                .requestMatchers(HttpMethod.POST, "/api/members/join").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members/oauth2").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members/change-tier").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/collabs").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/inquiries").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/charts/*/upvote").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/charts/*/play").permitAll()
                // Admin-only write operations
                .requestMatchers(HttpMethod.POST,    "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,     "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,   "/api/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,  "/api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DB-backed UserDetailsService.
     * Loads user from app_users table; role stored as "ADMIN" or "USER"
     * is prefixed with "ROLE_" by Spring Security convention.
     */
    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepo) {
        return username -> userRepo.findByUsernameIgnoreCase(username)
            .filter(com.mikstermedia.model.AppUser::isActive)
            .map(u -> User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())))
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
