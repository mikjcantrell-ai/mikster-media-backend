package com.mikstermedia.controller;

import com.mikstermedia.dto.SpotifySearchResult;
import com.mikstermedia.service.AiDiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the AI Discovery Feed.
 *
 * <p>Base path: {@code /api/ai-discovery}
 *
 * <ul>
 *   <li>GET  /api/ai-discovery/status   — is a fetch running? how many results?</li>
 *   <li>POST /api/ai-discovery/fetch    — start async multi-source discovery fetch (ADMIN)</li>
 *   <li>GET  /api/ai-discovery/results  — get results from the last completed fetch</li>
 * </ul>
 *
 * <p>Sources: Spotify keyword searches + YouTube keyword searches, both
 * targeting AI music tools (Suno, Udio, Stable Audio, etc.).
 * Results are deduped, sorted by release date, and pre-flagged if already imported.
 */
@RestController
@RequestMapping("/api/ai-discovery")
@RequiredArgsConstructor
@Slf4j
public class AiDiscoveryController {

    private final AiDiscoveryService aiDiscoveryService;

    /**
     * GET /api/ai-discovery/status  (public — shows progress indicator to admin)
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(aiDiscoveryService.getStatus());
    }

    /**
     * POST /api/ai-discovery/fetch  (ADMIN only — enforced by SecurityConfig)
     * Starts an async multi-source AI music discovery sweep.
     * Poll /status for progress (0–100%) and /results for data when done.
     */
    @PostMapping("/fetch")
    public ResponseEntity<Void> fetch() {
        log.info("Admin: AI Discovery feed fetch started");
        aiDiscoveryService.startDiscoveryFetch();
        return ResponseEntity.accepted().build();
    }

    /**
     * GET /api/ai-discovery/results  (public — admin reads the suggestion queue)
     * Returns the results from the most recent completed discovery fetch.
     * Results are sorted: new/not-imported first, then by release date desc.
     */
    @GetMapping("/results")
    public ResponseEntity<List<SpotifySearchResult>> results() {
        return ResponseEntity.ok(aiDiscoveryService.getLastResults());
    }
}
