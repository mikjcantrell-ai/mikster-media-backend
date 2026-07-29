package com.mikstermedia.service;

import com.mikstermedia.dto.SpotifySearchPage;
import com.mikstermedia.dto.SpotifySearchResult;
import com.mikstermedia.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SoundCloud track search service for Platform Discovery.
 *
 * <p>Uses the SoundCloud API v2 (the same endpoint used by SoundCloud's own web app).
 * Results are returned in the shared {@link SpotifySearchResult} DTO so the frontend
 * Platform Discovery component can handle them uniformly alongside Spotify and YouTube.
 *
 * <p>Configuration: set {@code soundcloud.client-id} in application.properties.
 * To obtain a client_id: inspect the network requests on soundcloud.com while logged in
 * — look for requests to api-v2.soundcloud.com that include a client_id query parameter.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SoundCloudService {

    private final TrackRepository trackRepository;

    @Value("${soundcloud.client-id:}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SC_SEARCH_URL = "https://api-v2.soundcloud.com/search/tracks";

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank();
    }

    /**
     * Search SoundCloud for tracks matching the given query.
     *
     * @param query search terms (e.g. "suno ai", "mozart ai music")
     * @param limit max results (SoundCloud supports up to 200)
     * @return page of results in shared DTO format
     */
    public SpotifySearchPage search(String query, int limit) {
        if (!isConfigured()) {
            log.warn("SoundCloud client_id is not configured — skipping search.");
            return new SpotifySearchPage(List.of(), 0, 0, limit);
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(SC_SEARCH_URL)
                    .queryParam("q", query)
                    .queryParam("client_id", clientId)
                    .queryParam("limit", Math.min(limit, 50))
                    .queryParam("linked_partitioning", "1")
                    .toUriString();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("collection")) {
                return new SpotifySearchPage(List.of(), 0, 0, limit);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> collection = (List<Map<String, Object>>) response.get("collection");
            List<SpotifySearchResult> results = new ArrayList<>();

            for (Map<String, Object> item : collection) {
                try {
                    SpotifySearchResult r = mapTrack(item);
                    if (r != null) results.add(r);
                } catch (Exception e) {
                    log.warn("Failed to parse SoundCloud track item: {}", e.getMessage());
                }
            }

            return new SpotifySearchPage(results, results.size(), 0, limit);

        } catch (Exception e) {
            log.error("SoundCloud search failed for query '{}': {}", query, e.getMessage());
            return new SpotifySearchPage(List.of(), 0, 0, limit);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private SpotifySearchResult mapTrack(Map<String, Object> item) throws Exception {
        Object idObj = item.get("id");
        String title  = (String) item.get("title");
        if (idObj == null || title == null || title.isBlank()) return null;

        String trackId = String.valueOf(idObj);

        // Artist info
        String artist    = "Unknown";
        String avatarUrl = null;
        Map<String, Object> user = (Map<String, Object>) item.get("user");
        if (user != null) {
            Object uname = user.get("username");
            if (uname != null) artist = uname.toString();
            avatarUrl = (String) user.get("avatar_url");
        }

        // Artwork — prefer track artwork, fall back to user avatar
        String artworkUrl = (String) item.get("artwork_url");
        if (artworkUrl == null && avatarUrl != null) artworkUrl = avatarUrl;
        if (artworkUrl != null) {
            // Upgrade from 100×100 "large" to 500×500 thumbnail
            artworkUrl = artworkUrl.replace("-large.jpg", "-t500x500.jpg");
        }

        // SoundCloud permalink
        String permalinkUrl = (String) item.get("permalink_url");

        // Build embed URL using the SoundCloud widget player
        String encodedApiUrl = URLEncoder.encode(
                "https://api.soundcloud.com/tracks/" + trackId, StandardCharsets.UTF_8);
        String embedUrl = "https://w.soundcloud.com/player/?url=" + encodedApiUrl
                + "&color=%23ff5500&auto_play=false&hide_related=false"
                + "&show_comments=false&show_user=true&show_reposts=false";

        // Stats
        int playbackCount = item.get("playback_count") != null
                ? ((Number) item.get("playback_count")).intValue() : 0;
        int duration = item.get("duration") != null
                ? ((Number) item.get("duration")).intValue() : 0;

        // Genre
        String genre = (String) item.get("genre");

        // Release date from created_at ISO timestamp
        String createdAt    = (String) item.get("created_at");
        String releaseDate  = (createdAt != null && createdAt.length() >= 10)
                ? createdAt.substring(0, 10) : "";

        // Normalise popularity to 0-100 range (SoundCloud doesn't have a popularity score)
        int popularity = Math.min(100, playbackCount / 500);

        SpotifySearchResult result = new SpotifySearchResult();
        result.setSpotifyId("sc_" + trackId);      // prefix avoids collision with Spotify IDs
        result.setTitle(title);
        result.setArtist(artist);
        result.setAlbum(genre != null && !genre.isBlank() ? genre : "SoundCloud");
        result.setAlbumImageUrl(artworkUrl);
        result.setSpotifyUrl(permalinkUrl);
        result.setEmbedUrl(embedUrl);
        result.setDurationMs(duration);
        result.setPopularity(popularity);
        result.setReleaseDate(releaseDate);
        result.setPlatformSource("SoundCloud");

        // Check if already imported (by permalink match or embed URL containing the track ID)
        final String fp = permalinkUrl;
        final String tid = trackId;
        boolean exists = fp != null && trackRepository.findAll().stream().anyMatch(t ->
                (t.getMediaUrl() != null && t.getMediaUrl().equals(fp)) ||
                (t.getEmbedUrl() != null && t.getEmbedUrl().contains(tid)));
        result.setAlreadyImported(exists);

        return result;
    }
}
