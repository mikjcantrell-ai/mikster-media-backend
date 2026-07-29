package com.mikstermedia.controller;

import com.mikstermedia.dto.SpotifySearchPage;
import com.mikstermedia.dto.TrackDTO;
import com.mikstermedia.dto.SpotifySearchResult;
import com.mikstermedia.model.Artist;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.service.SoundCloudService;
import com.mikstermedia.service.TrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for the SoundCloud Discovery feature.
 *
 * <p>Base path: {@code /api/soundcloud}
 *
 * <ul>
 *   <li>GET  /api/soundcloud/status  — is a client_id configured?</li>
 *   <li>GET  /api/soundcloud/search  — search SoundCloud tracks (public)</li>
 *   <li>POST /api/soundcloud/import  — import an approved track (ADMIN only)</li>
 * </ul>
 *
 * Security:
 *   GET  → public (no auth)
 *   POST → ADMIN only — enforced by SecurityConfig
 */
@RestController
@RequestMapping("/api/soundcloud")
@RequiredArgsConstructor
@Slf4j
public class SoundCloudController {

    private final SoundCloudService soundCloudService;
    private final TrackService      trackService;
    private final ArtistRepository  artistRepository;

    /**
     * GET /api/soundcloud/status
     * Returns whether the SoundCloud client_id is configured.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of("configured", soundCloudService.isConfigured()));
    }

    /**
     * GET /api/soundcloud/search
     *
     * @param q     search query
     * @param limit max results (default 10, max 50)
     */
    @GetMapping("/search")
    public ResponseEntity<SpotifySearchPage> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(soundCloudService.search(q, limit));
    }

    /**
     * POST /api/soundcloud/import (ADMIN only)
     *
     * <p>Converts an approved SoundCloud track into a Track in the library.
     * Auto-creates an Artist record if one doesn't already exist.
     * Sets platformSource = "SoundCloud" and uses the SoundCloud permalink as mediaUrl.
     */
    @PostMapping("/import")
    public ResponseEntity<Void> importTrack(@RequestBody SpotifySearchResult candidate) {

        // ── 1. Auto-create artist if needed ──────────────────────────────────
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
                    artist.setAiToolsUsed("SoundCloud Import");
                    if (candidate.getAlbumImageUrl() != null && !candidate.getAlbumImageUrl().isBlank()) {
                        artist.setImageUrl(candidate.getAlbumImageUrl());
                    }
                    artistRepository.save(artist);
                    log.info("Auto-created artist '{}' from SoundCloud import", name);
                }
            }
        }

        // ── 2. Create the Track ───────────────────────────────────────────────
        TrackDTO dto = new TrackDTO();
        dto.setTitle(candidate.getTitle());
        dto.setCreator(candidate.getArtist());
        dto.setMediaUrl(candidate.getSpotifyUrl());   // SoundCloud permalink URL
        dto.setPlatformSource("SoundCloud");
        dto.setEmbedUrl(candidate.getEmbedUrl());     // SoundCloud widget embed URL
        dto.setImageUrl(candidate.getAlbumImageUrl());
        dto.setAiToolsUsed("");                        // admin can fill in later
        dto.setGenre(candidate.getAlbum() != null
                && !candidate.getAlbum().equals("SoundCloud")
                ? candidate.getAlbum() : "");          // album field carries genre from SC
        dto.setFeaturedStatus(false);
        dto.setReleaseDate(candidate.getReleaseDate());

        try {
            trackService.createTrack(dto);
            log.info("Imported SoundCloud track '{}' by '{}'", candidate.getTitle(), candidate.getArtist());
        } catch (DataIntegrityViolationException e) {
            log.warn("Import skipped — duplicate SoundCloud track '{}': {}",
                     candidate.getTitle(), e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.noContent().build();
    }
}
