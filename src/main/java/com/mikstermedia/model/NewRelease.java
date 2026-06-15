package com.mikstermedia.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * NewRelease entity — spotlights recently released AI tracks.
 * Table: new_releases
 */
@Entity
@Table(name = "new_releases")
public class NewRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The track being spotlighted as a new release */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    /** Short editorial spotlight text (1-2 sentences) */
    @Column(name = "spotlight_text", columnDefinition = "TEXT")
    private String spotlightText;

    @Column(name = "featured_status")
    private boolean featuredStatus = false;

    /** Play count for this release */
    @Column(name = "play_count")
    private int playCount = 0;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getSpotlightText() { return spotlightText; }
    public void setSpotlightText(String spotlightText) { this.spotlightText = spotlightText; }

    public boolean isFeaturedStatus() { return featuredStatus; }
    public void setFeaturedStatus(boolean featuredStatus) { this.featuredStatus = featuredStatus; }

    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }
}
