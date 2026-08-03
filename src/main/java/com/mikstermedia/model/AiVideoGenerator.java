package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing an AI Video / Visualizer Generator tool for the Video Comparison Hub.
 */
@Entity
@Table(name = "ai_video_generators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiVideoGenerator {

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
    private String category; // e.g. "music_video", "visualizer", "short_form", "avatar"

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

    @Column(name = "max_resolution")
    private String maxResolution;

    @Column(name = "max_video_length")
    private String maxVideoLength;

    @Column(name = "has_beat_sync")
    private boolean hasBeatSync;

    @Column(name = "has_audio_reactive")
    private boolean hasAudioReactive;

    @Column(name = "has_singing_avatar")
    private boolean hasSingingAvatar;

    @Column(name = "has_stem_sync")
    private boolean hasStemSync;

    @Column(name = "commercial_rights_on_paid")
    private boolean commercialRightsOnPaid;

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
