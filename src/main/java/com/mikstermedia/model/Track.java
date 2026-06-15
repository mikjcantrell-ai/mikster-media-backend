package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

/**
 * JPA entity representing a curated / verified AI music track.
 *
 * <p>The {@code platform_source} field drives the frontend streaming embed
 * strategy: "Spotify" → iframe embed, "YouTube" → video ID embed,
 * "Apple" → Apple Music embed widget.
 *
 * <p>Hibernate will auto-create the {@code tracks} table on first startup
 * due to {@code spring.jpa.hibernate.ddl-auto=update}.
 *
 * <p>Natural keys enforced at the DB level:
 * <ul>
 *   <li>{@code uq_track_title_creator} — same title by the same creator is a duplicate</li>
 *   <li>{@code uq_track_media_url}     — the same streaming URL always means the same track</li>
 * </ul>
 */
@Entity
@Table(
    name = "tracks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_track_title_creator", columnNames = {"title", "creator"}),
        @UniqueConstraint(name = "uq_track_media_url",     columnNames = {"media_url"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {

    /** Auto-incremented surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display title of the AI-generated track. */
    @Column(nullable = false)
    private String title;

    /** Artist / creator name or alias. */
    @Column(nullable = false)
    private String creator;

    /**
     * Embeddable media URL or URI.
     * For Spotify: "spotify:track:<id>" or "https://open.spotify.com/...".
     * For YouTube: full watch URL or bare video ID.
     * For Apple: Apple Music album/track URL.
     */
    @Column(name = "media_url", nullable = false, length = 1024)
    private String mediaUrl;

    /**
     * Originating streaming platform.
     * Expected values: "Spotify", "YouTube", "Apple"
     */
    @Column(name = "platform_source", nullable = false)
    private String platformSource;

    /**
     * Comma-separated list of AI tools used to produce the track.
     * e.g. "Suno, Udio, Stable Audio"
     */
    @Column(name = "ai_tools_used", length = 512)
    private String aiToolsUsed;

    /** Musical genre tag (e.g. "Electronic", "Pop", "Ambient"). */
    @Column
    private String genre;

    /**
     * Admin-controlled display order for the homepage featured carousel.
     * Lower values appear first. Defaults to 0 (insertion order).
     */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    /**
     * Optional iframe-ready embed URL for inline playback.
     *
     * Platform formats:
     *   Spotify → https://open.spotify.com/embed/track/{id}?utm_source=oembed
     *   YouTube → https://www.youtube.com/embed/{videoId}
     *   Apple   → https://embed.music.apple.com/us/album/{name}/{id}?i={trackId}
     *
     * If null, the frontend falls back to the mediaUrl external link.
     */
    @Column(name = "embed_url", length = 1024)
    private String embedUrl;

    /**
     * Optional YouTube (or other video platform) URL for the music video
     * associated with this track. Entirely separate from {@code mediaUrl}:
     * a Spotify track can also have a YouTube music video here.
     *
     * <p>The frontend will embed this as an iframe on the featured-tracks
     * homepage section and link to it from the track card.
     *
     * Examples:
     *   YouTube watch URL:  https://www.youtube.com/watch?v=dQw4w9WgXcQ
     *   YouTube embed URL:  https://www.youtube.com/embed/dQw4w9WgXcQ
     */
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "ai_source_url", length = 1000)
    private String aiSourceUrl;

    /**
     * Optional cover art / album artwork image URL for this track.
     * Displayed in track cards on the Songs page, homepage featured section,
     * and charts. If null, the frontend falls back to a generated gradient placeholder.
     */
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    /** Spotify popularity score (0-100) imported and refreshed from the Spotify API. */
    @Column(name = "spotify_popularity")
    private Integer spotifyPopularity = 0;

    @Column(name = "last_fm_scrobbles")
    private Integer lastFmScrobbles = 0;

    @Column(name = "youtube_views")
    private Integer youtubeViews = 0;

    @Column(name = "suno_plays")
    private Integer sunoPlays = 0;

    @Column(name = "suno_likes")
    private Integer sunoLikes = 0;

    @Column(name = "udio_plays")
    private Integer udioPlays = 0;

    @Column(name = "udio_likes")
    private Integer udioLikes = 0;

    @Column(name = "chartmetric_score")
    private Integer chartmetricScore = 0;

    /**
     * Chartmetric internal track ID — resolved once via their search API using the
     * Spotify track ID, then stored here for all future stat refreshes.
     * Null means not yet looked up.
     */
    @Column(name = "chartmetric_id")
    private Long chartmetricId;


    // --- Weekly Snapshot Fields ---

    @Column(name = "last_week_spotify_popularity")
    private Integer lastWeekSpotifyPopularity = 0;

    @Column(name = "last_week_last_fm_scrobbles")
    private Integer lastWeekLastFmScrobbles = 0;

    @Column(name = "last_week_youtube_views")
    private Long lastWeekYoutubeViews = 0L;

    @Column(name = "last_week_tiktok_views")
    private Integer lastWeekTiktokViews = 0;

    @Column(name = "last_week_suno_plays")
    private Integer lastWeekSunoPlays = 0;

    @Column(name = "last_week_suno_likes")
    private Integer lastWeekSunoLikes = 0;

    @Column(name = "last_week_udio_plays")
    private Integer lastWeekUdioPlays = 0;

    @Column(name = "last_week_udio_likes")
    private Integer lastWeekUdioLikes = 0;

    @Column(name = "last_week_chartmetric_score")
    private Integer lastWeekChartmetricScore = 0;

    @Column(name = "tiktok_views")
    private Integer tiktokViews = 0;

    @Column(name = "featured_status")
    private boolean featuredStatus = false;

    /**
     * The date through which this track stays on the Featured homepage section.
     * Null means no expiry set. Admin can extend this by N days via POST /api/tracks/{id}/extend.
     * getFeaturedTracks() only returns tracks where featuredUntil >= today OR featuredUntil is null.
     */
    @Column(name = "featured_until")
    private LocalDate featuredUntil;

    /** Spotify release date imported and refreshed from the Spotify API. */
    @Column(name = "release_date")
    private String releaseDate;

    /** The prompt, parameters, or recipe used to generate the track in the AI tool. */
    @Column(name = "prompt_recipe", columnDefinition = "TEXT")
    private String promptRecipe;
}
