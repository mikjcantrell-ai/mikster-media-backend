package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing an AI Music Generator tool for the Comparison Hub.
 */
@Entity
@Table(name = "ai_generators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGenerator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String logo;

    @Column(columnDefinition = "TEXT")
    private String tagline;

    @Column(nullable = false)
    private String category; // "vocal", "instrumental", "both"

    @Column(name = "website_url", nullable = false)
    private String websiteUrl;

    private Double rating;

    @Column(columnDefinition = "TEXT")
    private String verdict;

    @Column(name = "free_tier", columnDefinition = "TEXT")
    private String freeTier;

    @Column(name = "pro_tier", columnDefinition = "TEXT")
    private String proTier;

    @Column(name = "premier_tier", columnDefinition = "TEXT")
    private String premierTier;

    @Column(name = "max_track_length")
    private String maxTrackLength;

    @Column(name = "has_stems")
    private boolean hasStems;

    @Column(name = "has_inpainting")
    private boolean hasInpainting;

    @Column(name = "has_custom_lyrics")
    private boolean hasCustomLyrics;

    @Column(name = "has_midi_export")
    private boolean hasMidiExport;

    @Column(name = "commercial_rights_on_paid")
    private boolean commercialRightsOnPaid;

    @Column(name = "audio_quality")
    private String audioQuality;

    @Column(name = "best_for_tags", columnDefinition = "TEXT")
    private String bestForTags; // Comma-separated list of tags

    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder.Default
    @Column(name = "url_reachable")
    private boolean urlReachable = true;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;
}
