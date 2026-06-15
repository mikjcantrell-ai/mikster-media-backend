package com.mikstermedia.controller;

import com.mikstermedia.service.ChartmetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for the Chartmetric integration.
 *
 * <p>Base path: {@code /api/chartmetric}
 *
 * <ul>
 *   <li>GET  /api/chartmetric/status                  — is a refresh token configured?</li>
 *   <li>POST /api/chartmetric/refresh/track/{id}      — refresh one track (ADMIN)</li>
 *   <li>POST /api/chartmetric/refresh/tracks          — bulk-refresh all tracks (ADMIN)</li>
 *   <li>GET  /api/chartmetric/refresh/status          — poll bulk-refresh progress</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/chartmetric")
@RequiredArgsConstructor
@Slf4j
public class ChartmetricController {

    private final ChartmetricService chartmetricService;

    /**
     * GET /api/chartmetric/status
     * Returns whether the Chartmetric refresh token is configured.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of("configured", chartmetricService.isConfigured()));
    }

    /**
     * POST /api/chartmetric/refresh/track/{id}  (ADMIN only — enforced by SecurityConfig)
     * Refreshes Chartmetric score for a single track.
     * Resolves Chartmetric ID from Spotify URL if not already stored.
     */
    @PostMapping("/refresh/track/{id}")
    public ResponseEntity<Map<String, Object>> refreshTrack(@PathVariable Long id) {
        log.info("Admin: Chartmetric refresh requested for track id={}", id);
        return ResponseEntity.ok(chartmetricService.refreshTrack(id));
    }

    /**
     * POST /api/chartmetric/refresh/tracks  (ADMIN only — enforced by SecurityConfig)
     * Starts an async bulk-refresh of Chartmetric scores for all tracks.
     * Rate-limited to ~1 req/sec. Poll /refresh/status for progress.
     */
    @PostMapping("/refresh/tracks")
    public ResponseEntity<Void> refreshAllTracks() {
        log.info("Admin: Chartmetric bulk refresh started");
        chartmetricService.startRefreshAllTracks();
        return ResponseEntity.accepted().build();
    }

    /**
     * GET /api/chartmetric/refresh/status
     * Returns progress of the current (or last completed) bulk refresh job.
     */
    @GetMapping("/refresh/status")
    public ResponseEntity<Map<String, Object>> refreshStatus() {
        return ResponseEntity.ok(chartmetricService.getRefreshStatus());
    }
}
