package com.mikstermedia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Artist entity — represents an AI music creator/artist profile.
 * Table: artists
 *
 * <p>Natural key: {@code name} — artist names must be unique across the platform.
 */
@Entity
@Table(
    name = "artists",
    uniqueConstraints = @UniqueConstraint(name = "uq_artist_name", columnNames = "name")
)
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String country;

    /** Comma-separated AI tools used by this artist (e.g. "Suno v3, Udio") */
    @Column(name = "ai_tools_used")
    private String aiToolsUsed;

    /** Primary genre association */
    private String primaryGenre;

    /** External URL to an artist avatar/photo */
    @Column(name = "image_url")
    private String imageUrl;

    /** Social/streaming profile link */
    @Column(name = "profile_url")
    private String profileUrl;

    /** Artist's own website (e.g. https://artist.com) */
    @Column(name = "website_url", length = 1024)
    private String websiteUrl;

    @Column(name = "featured_status")
    private boolean featuredStatus = false;

    /** Timestamp when featuredStatus was last set to true — used for 14-day auto-expiry */
    @Column(name = "featured_since")
    private LocalDateTime featuredSince;

    /** Admin-controlled display order within the featured carousel (lower = first) */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "monthly_listeners")
    private int monthlyListeners = 0;

    /** Number of tracks by this artist in the database */
    @org.hibernate.annotations.Formula("(SELECT COUNT(*) FROM tracks t WHERE t.creator = name)")
    private int trackCount = 0;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAiToolsUsed() { return aiToolsUsed; }
    public void setAiToolsUsed(String aiToolsUsed) { this.aiToolsUsed = aiToolsUsed; }

    public String getPrimaryGenre() { return primaryGenre; }
    public void setPrimaryGenre(String primaryGenre) { this.primaryGenre = primaryGenre; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public boolean isFeaturedStatus() { return featuredStatus; }
    public void setFeaturedStatus(boolean featuredStatus) { this.featuredStatus = featuredStatus; }

    public LocalDateTime getFeaturedSince() { return featuredSince; }
    public void setFeaturedSince(LocalDateTime featuredSince) { this.featuredSince = featuredSince; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder == null ? 0 : displayOrder; }

    public int getMonthlyListeners() { return monthlyListeners; }
    public void setMonthlyListeners(int monthlyListeners) { this.monthlyListeners = monthlyListeners; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }
}
