package com.mikstermedia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Community member entity — separate from AppUser (admin accounts).
 * Stores public-facing membership registrations from the /join page.
 */
@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String username;

    @Column(name = "auth_provider", length = 20)
    private String authProvider = "LOCAL";

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "password_hash")
    private String passwordHash;

    /** LISTENER | CREATOR | PRODUCER */
    @Column(name = "membership_tier", length = 20)
    private String membershipTier = "LISTENER";

    @Column(name = "primary_ai_tool")
    private String primaryAiTool;

    @Column(name = "genre_interest")
    private String genreInterest;

    @Column(name = "newsletter_opt_in")
    private boolean newsletterOptIn = true;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column
    private boolean active = true;

    @PrePersist
    protected void onCreate() { this.joinedAt = LocalDateTime.now(); }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getDisplayName()              { return displayName; }
    public void setDisplayName(String v)        { this.displayName = v; }

    public String getEmail()                    { return email; }
    public void setEmail(String v)              { this.email = v; }

    public String getUsername()                 { return username; }
    public void setUsername(String v)           { this.username = v; }

    public String getAuthProvider()             { return authProvider; }
    public void setAuthProvider(String v)       { this.authProvider = v; }

    public String getProviderId()               { return providerId; }
    public void setProviderId(String v)         { this.providerId = v; }

    public String getPasswordHash()             { return passwordHash; }
    public void setPasswordHash(String v)       { this.passwordHash = v; }

    public String getMembershipTier()           { return membershipTier; }
    public void setMembershipTier(String v)     { this.membershipTier = v; }

    public String getPrimaryAiTool()            { return primaryAiTool; }
    public void setPrimaryAiTool(String v)      { this.primaryAiTool = v; }

    public String getGenreInterest()            { return genreInterest; }
    public void setGenreInterest(String v)      { this.genreInterest = v; }

    public boolean isNewsletterOptIn()          { return newsletterOptIn; }
    public void setNewsletterOptIn(boolean v)   { this.newsletterOptIn = v; }

    public LocalDateTime getJoinedAt()          { return joinedAt; }

    public boolean isActive()                   { return active; }
    public void setActive(boolean v)            { this.active = v; }
}
