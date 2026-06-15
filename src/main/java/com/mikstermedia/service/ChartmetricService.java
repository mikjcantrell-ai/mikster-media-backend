package com.mikstermedia.service;

import com.mikstermedia.model.Track;
import com.mikstermedia.repository.TrackRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

/**
 * Chartmetric integration service.
 *
 * <p>Chartmetric provides cross-platform streaming intelligence including
 * Spotify stream estimates, momentum scores, and historical stats.
 *
 * <h2>Setup (when you receive your refresh token from Chartmetric)</h2>
 * <ol>
 *   <li>Email hi@chartmetric.com to request API access</li>
 *   <li>Set {@code chartmetric.refresh-token} in application.properties</li>
 *   <li>Call POST /api/chartmetric/refresh/tracks to populate all tracks</li>
 * </ol>
 *
 * <h2>Token Flow</h2>
 * <ol>
 *   <li>Exchange refresh-token via POST /api/token → short-lived access token (1 hour)</li>
 *   <li>Use access token as Bearer on all subsequent requests</li>
 *   <li>Auto-renew 60s before expiry</li>
 * </ol>
 *
 * <h2>Track Lookup Flow</h2>
 * <ol>
 *   <li>Search by title/artist → GET /api/search/track → match by Spotify ID → Chartmetric track ID</li>
 *   <li>Store Chartmetric ID in {@code tracks.chartmetric_id} to avoid repeated lookups</li>
 *   <li>Fetch stats → GET /api/track/{cm_id}/stat/spotify → extract sp_popularity</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChartmetricService {

    private static final String CM_BASE = "https://api.chartmetric.com";

    @Value("${chartmetric.refresh-token:}")
    private String refreshToken;

    private final TrackRepository trackRepository;
    private final ObjectMapper    objectMapper = new ObjectMapper();
    private final RestTemplate    restTemplate = new RestTemplate();

    // ── Token state ──────────────────────────────────────────────────────────
    private String  accessToken    = null;
    private Instant tokenExpiresAt = Instant.EPOCH;

    // ── Bulk refresh progress ─────────────────────────────────────────────────
    private volatile boolean refreshRunning   = false;
    private volatile int     refreshTotal     = 0;
    private volatile int     refreshCompleted = 0;
    private volatile String  refreshError     = null;

    // ── Status ────────────────────────────────────────────────────────────────

    /** Returns true when a refresh token is configured in application.properties. */
    public boolean isConfigured() {
        return refreshToken != null && !refreshToken.isBlank();
    }

    public Map<String, Object> getRefreshStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("configured",  isConfigured());
        status.put("running",     refreshRunning);
        status.put("total",       refreshTotal);
        status.put("completed",   refreshCompleted);
        if (refreshError != null) status.put("error", refreshError);
        return status;
    }

    // ── Token management ──────────────────────────────────────────────────────

    /**
     * Returns a valid Bearer access token, minting a new one if needed.
     * Throws {@link IllegalStateException} if no refresh token is configured.
     */
    private synchronized String getAccessToken() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                "Chartmetric refresh token not configured. " +
                "Set chartmetric.refresh-token in application.properties.");
        }
        // Reuse token if still valid (with 60s safety buffer)
        if (accessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            return accessToken;
        }
        log.info("Minting new Chartmetric access token...");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = Map.of("refreshtoken", refreshToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                CM_BASE + "/api/token", request, String.class);
            JsonNode root      = objectMapper.readTree(response.getBody());
            accessToken        = root.get("token").asText();
            int expiresIn      = root.has("expires_in") ? root.get("expires_in").asInt(3600) : 3600;
            tokenExpiresAt     = Instant.now().plusSeconds(expiresIn);
            log.info("Chartmetric access token minted, expires in {}s", expiresIn);
            return accessToken;
        } catch (Exception e) {
            log.error("Failed to mint Chartmetric access token: {}", e.getMessage());
            throw new RuntimeException("Chartmetric token exchange failed: " + e.getMessage(), e);
        }
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(getAccessToken());
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        return h;
    }

    // ── Chartmetric ID Lookup ─────────────────────────────────────────────────

    /**
     * Looks up the Chartmetric track ID for a given Spotify track ID.
     *
     * <p>Searches by track title, then matches results by Spotify ID.
     *
     * @param spotifyId  Spotify track ID (e.g. "4uLU6hMCjMI75M1A2tKUQC")
     * @param trackTitle Track title used as search query
     * @return Chartmetric track ID, or null if not found
     */
    public Long lookupChartmetricId(String spotifyId, String trackTitle) {
        if (!isConfigured() || spotifyId == null || spotifyId.isBlank()) return null;
        try {
            String encoded = java.net.URLEncoder.encode(trackTitle, "UTF-8");
            String url = CM_BASE + "/api/search/track?q=" + encoded + "&limit=5&type=track";
            HttpEntity<Void> req = new HttpEntity<>(bearerHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
            JsonNode root  = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("obj").path("tracks");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    JsonNode spotifyIds = item.path("spotify_track_ids");
                    if (spotifyIds.isArray()) {
                        for (JsonNode sid : spotifyIds) {
                            if (spotifyId.equals(sid.asText())) {
                                long cmId = item.get("id").asLong();
                                log.info("Chartmetric ID {} found for Spotify ID {}", cmId, spotifyId);
                                return cmId;
                            }
                        }
                    }
                }
            }
            log.debug("No Chartmetric match for Spotify ID {} (title: {})", spotifyId, trackTitle);
            return null;
        } catch (Exception e) {
            log.warn("Chartmetric ID lookup failed for '{}': {}", trackTitle, e.getMessage());
            return null;
        }
    }

    // ── Stats Fetch ───────────────────────────────────────────────────────────

    /**
     * Fetches the latest Spotify popularity score from Chartmetric for a track.
     *
     * <p>Uses GET /api/track/{cm_id}/stat/spotify — returns the most recent
     * {@code sp_popularity} value from the timeseries data.
     *
     * <p>When Chartmetric exposes granular stream count estimates in future
     * API versions, update this method to extract those fields instead.
     *
     * @param chartmetricId  Chartmetric internal track ID
     * @return popularity score (0–100), or -1 if unavailable / error
     */
    public int fetchChartmetricScore(long chartmetricId) {
        if (!isConfigured()) return -1;
        try {
            String url = CM_BASE + "/api/track/" + chartmetricId + "/stat/spotify";
            HttpEntity<Void> req = new HttpEntity<>(bearerHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
            JsonNode root       = objectMapper.readTree(response.getBody());
            JsonNode timeseries = root.path("obj");
            if (timeseries.isArray() && timeseries.size() > 0) {
                JsonNode latest = timeseries.get(timeseries.size() - 1);
                if (latest.has("sp_popularity")) {
                    return latest.get("sp_popularity").asInt(0);
                }
            }
            log.debug("No sp_popularity in Chartmetric stats for cm_id={}", chartmetricId);
            return 0;
        } catch (Exception e) {
            log.warn("Chartmetric stats fetch failed for cm_id={}: {}", chartmetricId, e.getMessage());
            return -1;
        }
    }

    // ── Single Track Refresh ──────────────────────────────────────────────────

    /**
     * Refreshes Chartmetric data for a single track.
     *
     * <p>Resolves the Chartmetric ID from the track's Spotify URL if not stored,
     * then fetches and persists the latest score.
     *
     * @param trackId  internal Track entity ID
     * @return result summary map
     */
    public Map<String, Object> refreshTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
            .orElseThrow(() -> new RuntimeException("Track not found: " + trackId));

        if (!isConfigured()) {
            return Map.of("status", "skipped", "reason",
                "Chartmetric not configured — set chartmetric.refresh-token in application.properties");
        }

        // Resolve Chartmetric ID if not already stored
        Long cmId = track.getChartmetricId();
        if (cmId == null) {
            String spotifyId = extractSpotifyId(track.getMediaUrl());
            if (spotifyId != null) {
                cmId = lookupChartmetricId(spotifyId, track.getTitle());
                if (cmId != null) {
                    track.setChartmetricId(cmId);
                    trackRepository.save(track);
                }
            }
        }

        if (cmId == null) {
            return Map.of("status", "skipped", "reason",
                "Could not resolve Chartmetric ID for: " + track.getTitle());
        }

        int score = fetchChartmetricScore(cmId);
        if (score >= 0) {
            track.setChartmetricScore(score);
            trackRepository.save(track);
            log.info("Updated chartmetricScore={} for track '{}'", score, track.getTitle());
            return Map.of("status", "updated", "chartmetricId", cmId, "chartmetricScore", score);
        } else {
            return Map.of("status", "error", "reason", "Stats fetch returned no data");
        }
    }

    // ── Bulk Refresh ──────────────────────────────────────────────────────────

    /**
     * Starts an async bulk refresh of Chartmetric scores for all tracks.
     * Uses virtual threads and respects the ~1 req/sec free-tier rate limit.
     * Progress can be polled via GET /api/chartmetric/refresh/status.
     */
    public void startRefreshAllTracks() {
        if (refreshRunning) {
            log.warn("Chartmetric bulk refresh already running");
            return;
        }
        List<Track> tracks = trackRepository.findAll();
        refreshTotal     = tracks.size();
        refreshCompleted = 0;
        refreshRunning   = true;
        refreshError     = null;

        Thread t = new Thread(() -> {
            try {
                for (Track track : tracks) {
                    try {
                        refreshTrack(track.getId());
                    } catch (Exception e) {
                        log.warn("Chartmetric refresh skipped for track {}: {}", track.getId(), e.getMessage());
                    }
                    refreshCompleted++;
                    // Respect rate limits — ~1 req/sec on free tier
                    Thread.sleep(1100);
                }
                log.info("Chartmetric bulk refresh complete: {}/{} tracks", refreshCompleted, refreshTotal);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                refreshError = "Refresh interrupted";
            } catch (Exception e) {
                refreshError = e.getMessage();
                log.error("Chartmetric bulk refresh error: {}", e.getMessage());
            } finally {
                refreshRunning = false;
            }
        });
        t.setDaemon(true);
        t.setName("chartmetric-bulk-refresh");
        t.start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Extracts the Spotify track ID from various URL/URI formats:
     *   https://open.spotify.com/track/4uLU6hMCjMI75M1A2tKUQC
     *   spotify:track:4uLU6hMCjMI75M1A2tKUQC
     */
    String extractSpotifyId(String mediaUrl) {
        if (mediaUrl == null) return null;
        if (mediaUrl.startsWith("spotify:track:")) {
            return mediaUrl.substring("spotify:track:".length());
        }
        if (mediaUrl.contains("open.spotify.com/track/")) {
            String[] parts = mediaUrl.split("/track/");
            if (parts.length > 1) {
                String id = parts[1];
                int q = id.indexOf('?');
                return q > 0 ? id.substring(0, q) : id;
            }
        }
        return null;
    }
}
