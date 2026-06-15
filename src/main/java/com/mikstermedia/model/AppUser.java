package com.mikstermedia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AppUser entity — platform users stored in aimusic.db.
 *
 * Roles:
 *   ADMIN — full CRUD access via admin dashboard
 *   USER  — future public-facing login (music favoriting, playlists, etc.)
 *
 * Password is stored as a BCrypt hash — never in plaintext.
 * The passwordHash field is excluded from API responses via @JsonIgnore
 * so it is never sent to the frontend.
 */
@Entity
@Table(name = "app_users",
       uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** BCrypt-hashed password — never returned to the frontend. */
    @Column(name = "password_hash", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String passwordHash;

    @Column(length = 128)
    private String email;

    @Column(nullable = false, length = 16)
    private String role = "USER"; // ADMIN or USER

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
