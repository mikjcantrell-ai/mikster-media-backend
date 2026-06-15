package com.mikstermedia.model;

import jakarta.persistence.*;

/**
 * Genre entity — represents a music genre category.
 * Table: genres
 */
@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** CSS hex color for the genre chip/card (e.g. "#b06dff") */
    @Column(name = "color_hex")
    private String colorHex;

    /** Emoji icon for the genre (e.g. "🎵") */
    @Column(name = "icon_emoji")
    private String iconEmoji;

    /** Track count — updated by service layer */
    @Column(name = "track_count")
    private int trackCount = 0;

    @Column(name = "featured_status")
    private Boolean featuredStatus = false;

    @Column(name = "featured_since")
    private java.time.LocalDateTime featuredSince;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

    public int getTrackCount() { return trackCount; }
    public void setTrackCount(int trackCount) { this.trackCount = trackCount; }

    public Boolean isFeaturedStatus() { return featuredStatus != null && featuredStatus; }
    public void setFeaturedStatus(Boolean featuredStatus) { this.featuredStatus = featuredStatus; }

    public java.time.LocalDateTime getFeaturedSince() { return featuredSince; }
    public void setFeaturedSince(java.time.LocalDateTime featuredSince) { this.featuredSince = featuredSince; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
