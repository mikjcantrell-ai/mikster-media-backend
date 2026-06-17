package com.mikstermedia.controller;

import com.mikstermedia.dto.SpotifySearchPage;
import com.mikstermedia.dto.SpotifySearchResult;
import com.mikstermedia.dto.TrackDTO;
import com.mikstermedia.model.Artist;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.service.SpotifyService;
import com.mikstermedia.service.TrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the Spotify Discovery admin feature.
 *
 * <p>Base path: {@code /api/spotify}
 *
 * <ul>
 *   <li>GET  /api/spotify/status  — returns whether credentials are configured</li>
 *   <li>GET  /api/spotify/search  — searches Spotify and returns candidate tracks</li>
 *   <li>POST /api/spotify/import  — imports a reviewed track (ADMIN only)</li>
 * </ul>
 *
 * Security:
 *   GET  → public (no auth)
 *   POST → ADMIN only — enforced by SecurityConfig
 */
@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
@Slf4j
public class SpotifyController {

    private final SpotifyService   spotifyService;
    private final TrackService     trackService;
    private final ArtistRepository artistRepository;

    /** Returns Spotify configuration status, including whether user OAuth is connected. */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "configured",     spotifyService.isConfigured(),
            "userConnected",  spotifyService.isUserAuthenticated()
        ));
    }

    // ── OAuth Authorization Code flow ─────────────────────────────────────

    /**
     * GET /api/spotify/auth/login
     * Generates the Spotify authorization URL and redirects the browser there.
     * After approval, Spotify calls back to /api/spotify/auth/callback.
     *
     * <p><strong>Prerequisite:</strong> Add
     * {@code http://localhost:8081/api/spotify/auth/callback} as a Redirect URI
     * in your <a href="https://developer.spotify.com/dashboard">Spotify Dashboard</a>.
     */
    @GetMapping("/auth/login")
    public void login(HttpServletResponse response) throws IOException {
        String authUrl = spotifyService.buildAuthorizationUrl();
        response.sendRedirect(authUrl);
    }

    /**
     * GET /api/spotify/auth/callback
     * Spotify redirects here after the user approves access.
     * Exchanges the authorization code for tokens, then redirects the browser
     * back to the admin Spotify Discovery page.
     */
    @GetMapping("/auth/callback")
    public void callback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {
        try {
            spotifyService.exchangeCodeForTokens(code);
            log.info("Spotify OAuth completed successfully");
            response.sendRedirect("https://localhost:4200/admin/spotify-discovery?spotifyConnected=true");
        } catch (Exception e) {
            log.error("Spotify OAuth callback failed: {}", e.getMessage());
            response.sendRedirect("https://localhost:4200/admin/spotify-discovery?spotifyConnected=false");
        }
    }

    /**
     * GET /api/spotify/trending?top=20
     * Fans out across 7 AI search queries, deduplicates, and returns the
     * top-N results sorted by Spotify popularity descending.
     * Takes ~1-2 seconds (7 requests × 150ms sleep).
     */
    @GetMapping("/trending")
    public ResponseEntity<List<SpotifySearchResult>> trending(
            @RequestParam(defaultValue = "10") int top) {
        if (!spotifyService.isUserAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok()
            .header("Cache-Control", "no-store, no-cache, must-revalidate")
            .body(spotifyService.trending(top));
    }

    /** Searches Spotify and returns paginated candidates for admin review. */
    @GetMapping("/search")
    public ResponseEntity<SpotifySearchPage> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0")  int offset) {
        return ResponseEntity.ok()
            .header("Cache-Control", "no-store, no-cache, must-revalidate")
            .body(spotifyService.search(q, limit, offset));
    }

    /**
     * POST /api/spotify/import (ADMIN only)
     *
     * <p>Converts an approved Spotify track into a Track in the library.
     * Also auto-creates Artist record(s) if they don't already exist,
     * using the album art as a placeholder image.
     *
     * <p>Handles multi-artist tracks (e.g. "Artist A, Artist B") by creating
     * a separate Artist row for each name.
     */
    @PostMapping("/import")
    public ResponseEntity<Void> importTrack(@RequestBody SpotifySearchResult candidate) {

        // ── 1. Auto-create artist(s) ─────────────────────────────────────────
        String artistField = candidate.getArtist();
        if (artistField != null && !artistField.isBlank()) {
            for (String raw : artistField.split(",")) {
                String name = raw.trim();
                if (name.isBlank()) continue;

                boolean exists = artistRepository
                    .findByNameContainingIgnoreCase(name)
                    .stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(name));

                if (!exists) {
                    Artist artist = new Artist();
                    artist.setName(name);
                    artist.setAiToolsUsed("Spotify Import");
                    // Use album art as a starting image — admin can replace later
                    if (candidate.getAlbumImageUrl() != null && !candidate.getAlbumImageUrl().isBlank()) {
                        artist.setImageUrl(candidate.getAlbumImageUrl());
                    }
                    artistRepository.save(artist);
                    log.info("Auto-created artist '{}' from Spotify import", name);
                }
            }
        }

        // ── 2. Create the Track ──────────────────────────────────────────────
        TrackDTO dto = new TrackDTO();
        dto.setTitle(candidate.getTitle());
        dto.setCreator(candidate.getArtist());
        dto.setMediaUrl(candidate.getSpotifyUrl());
        dto.setPlatformSource("Spotify");
        dto.setEmbedUrl(candidate.getEmbedUrl());
        dto.setImageUrl(candidate.getAlbumImageUrl());
        dto.setAiToolsUsed("");       // admin can fill in later
        
        if (candidate.getPrimaryArtistId() != null && !candidate.getPrimaryArtistId().isBlank()) {
            String genre = spotifyService.fetchArtistGenre(candidate.getPrimaryArtistId());
            if (genre == null || genre.isBlank()) {
                // Fallback to local Artist DB
                Artist localArtist = artistRepository.findByNameIgnoreCase(candidate.getArtist()).orElse(null);
                if (localArtist != null && localArtist.getPrimaryGenre() != null && !localArtist.getPrimaryGenre().isBlank()) {
                    genre = localArtist.getPrimaryGenre();
                }
            }
            dto.setGenre(genre != null ? genre : "");
        } else {
            dto.setGenre("");
        }
        
        dto.setFeaturedStatus(false);
        dto.setSpotifyPopularity(candidate.getPopularity());
        dto.setReleaseDate(candidate.getReleaseDate());

        // ── 3. Capture YouTube link if present ───────────────────────────────
        // The frontend may send a youtubeUrl alongside a Spotify result (e.g. when
        // the result was cross-referenced or the platform is YouTube via this endpoint).
        if (candidate.getYoutubeUrl() != null && !candidate.getYoutubeUrl().isBlank()) {
            dto.setVideoUrl(candidate.getYoutubeUrl());
            // If the mediaUrl wasn't set from Spotify (e.g. YouTube-only result routed
            // through this endpoint), fall back to the YouTube URL as the primary link.
            if (dto.getMediaUrl() == null || dto.getMediaUrl().isBlank()) {
                dto.setMediaUrl(candidate.getYoutubeUrl());
            }
        }

        trackService.createTrack(dto);
        return ResponseEntity.noContent().build();
    }

    // ── Metadata Refresh ───────────────────────────────────────────────────────

    /** POST /api/spotify/refresh/track/{id} — refreshes title, creator, artwork for one track. */
    @PostMapping("/refresh/track/{id}")
    public ResponseEntity<Map<String, Object>> refreshTrack(@PathVariable Long id) {
        return ResponseEntity.ok(spotifyService.refreshTrack(id));
    }

    /** POST /api/spotify/refresh/artist/{id} — refreshes image, followers, genre for one artist. */
    @PostMapping("/refresh/artist/{id}")
    public ResponseEntity<Map<String, Object>> refreshArtist(@PathVariable Long id) {
        return ResponseEntity.ok(spotifyService.refreshArtist(id));
    }

    /** POST /api/spotify/refresh/tracks — start bulk-refresh all Spotify tracks. */
    @PostMapping("/refresh/tracks")
    public ResponseEntity<Void> refreshAllTracks() {
        spotifyService.startRefreshAllTracks();
        return ResponseEntity.accepted().build();
    }

    /** POST /api/spotify/refresh/artists — start bulk-refresh all artists. */
    @PostMapping("/refresh/artists")
    public ResponseEntity<Void> refreshAllArtists() {
        spotifyService.startRefreshAllArtists();
        return ResponseEntity.accepted().build();
    }

    /** GET /api/spotify/refresh/status — returns progress of active refresh jobs. */
    @GetMapping("/refresh/status")
    public ResponseEntity<Map<String, Object>> getRefreshStatus() {
        return ResponseEntity.ok(spotifyService.getRefreshStatus());
    }
}
