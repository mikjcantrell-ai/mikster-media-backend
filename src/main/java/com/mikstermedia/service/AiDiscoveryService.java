package com.mikstermedia.service;

import com.mikstermedia.dto.SpotifySearchResult;
import com.mikstermedia.dto.TrackDTO;
import com.mikstermedia.model.Artist;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.TrackRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.mikstermedia.dto.SpotifySearchPage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Discovery Feed Service.
 *
 * <p>Aggregates new/trending AI-generated music from multiple sources into
 * a unified "Suggested Imports" queue for admin review.
 *
 * <h2>Sources</h2>
 * <ul>
 *   <li><strong>Spotify</strong> — keyword searches for AI tool names and genre tags
 *       (suno, udio, stable audio, ai generated, etc.) across multiple queries,
 *       deduped and sorted by release date + popularity</li>
 *   <li><strong>YouTube</strong> — searches for recent AI music uploads using the
 *       YouTube Data API v3 with date ordering</li>
 *   <li><strong>Suno (song pages)</strong> — individual Suno song URLs can still be
 *       scraped for metadata; the explore/feed API is blocked (503)</li>
 * </ul>
 *
 * <h2>Why not Suno/Udio explore pages?</h2>
 * <p>Both platforms are fully client-side rendered (Next.js / React). The raw HTML
 * contains no song data — it only loads after JavaScript executes in a browser.
 * Suno's internal studio-api.suno.ai is also suspended (returns 503).
 * Individual song page scraping still works and is used in {@link SunoScraperService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiDiscoveryService {

    // ── Spotify: artist-name targeted queries ─────────────────────────────────
    // Using `artist:` prefix so Spotify matches the ARTIST field, not just any
    // word in a track title/playlist description. This dramatically reduces noise.
    private static final List<String> SPOTIFY_QUERIES = List.of(
        // Established generators
        "artist:suno",
        "artist:udio",
        "artist:\"stable audio\"",
        "artist:\"ai generated\"",
        "artist:\"suno ai\"",
        "artist:\"udio ai\"",
        "artist:musicgen",
        "artist:\"artificial intelligence\" music",
        "artist:\"ai music\"",
        // Newer generators (2025-2026 wave)
        "artist:\"mozart ai\"",
        "artist:mozartai",
        "artist:sonauto",
        "artist:riffusion",
        "artist:\"google musiclm\"",
        "artist:beatoven",
        "artist:loudly",
        "artist:\"meta musicgen\"",
        // User's explicit band
        "artist:\"confetti weather\""
    );

    // ── YouTube: channel/title queries that explicitly name the AI tool used ───
    // Quoted phrases require the exact string; avoids loose matches like
    // "Santali Romantic AI Cover Song" which aren't AI-generated compositions.
    private static final List<String> YOUTUBE_QUERIES = List.of(
        // Established generators
        "\"suno ai\" original song 2026",
        "\"udio ai\" original music 2026",
        "\"stable audio\" generated music",
        "\"ai generated\" original song 2026",
        "musicgen ai music release",
        // Newer generators (2025-2026 wave)
        "\"mozart ai\" original song",
        "\"sonauto\" ai music track",
        "\"riffusion\" generated music",
        "\"google lyria\" ai music",
        "\"beatoven\" ai track",
        "\"meta musicgen\" music 2026"
    );

    private static final List<String> SOUNDCLOUD_QUERIES = List.of(
        "\"suno ai\"",
        "\"udio ai\"",
        "\"stable audio\"",
        "\"ai generated music\"",
        "\"mozart ai\"",
        "\"sonauto\"",
        "\"riffusion\""
    );

    private static final List<String> ITUNES_QUERIES = List.of(
        "suno ai",
        "udio ai",
        "stable audio",
        "ai generated music",
        "mozart ai",
        "sonauto",
        "riffusion"
    );

    private static final List<String> REDDIT_SUBREDDITS = List.of(
        "SunoAI",
        "UdioMusic",
        "AIMusicCreate"
    );

    // ── AI indicator terms for post-filter ────────────────────────────────────
    // A result must match at least one ARTIST term AND/OR one TITLE term.
    private static final List<String> AI_ARTIST_TERMS = List.of(
        // Established generators
        "suno", "udio", "stable audio", "musicgen", "ai generated",
        "artificial intelligence", "ai music", "dawnai", "cosmicai",
        "metalmind", "ai-generated", "generative ai", "openai",
        "elevenlabs", "boomy", "aiva", "amper", "mubert", "soundraw",
        "beatoven", "soundful", "loudly", "ecrett", "melobytes",
        // Newer generators (2025-2026 wave)
        "mozart ai", "mozartai", "sonauto", "riffusion",
        "google musiclm", "google lyria", "lyria",
        "meta musicgen", "udio 2.0", "chirp",
        // Specific whitelisted artists
        "confetti weather"
    );

    private static final List<String> AI_TITLE_TERMS = List.of(
        // Established terms
        "suno ai", "udio ai", "stable audio", "ai generated", "ai-generated",
        "musicgen", "made with ai", "created with ai", "ai music",
        "generated by ai", "artificial intelligence music", "ai composed",
        "ai song", "ai cover",
        // Newer generator brand names in titles
        "mozart ai", "sonauto", "riffusion", "google lyria", "lyria ai",
        "meta musicgen", "beatoven ai", "generated by suno", "generated by udio"
    );

    private static final String SPOTIFY_TOKEN_URL  = "https://accounts.spotify.com/api/token";
    private static final String SPOTIFY_SEARCH_URL = "https://api.spotify.com/v1/search";
    private static final String YT_SEARCH_URL      = "https://www.googleapis.com/youtube/v3/search";

    @Value("${spotify.client-id:}")      private String spotifyClientId;
    @Value("${spotify.client-secret:}") private String spotifyClientSecret;
    @Value("${youtube.api-key:}")        private String youtubeApiKey;

    private final TrackRepository   trackRepository;
    private final TrackService       trackService;
    private final ArtistRepository   artistRepository;
    private final SoundCloudService  soundCloudService;
    private final LanguageDetectionService languageDetectionService;
    private final ObjectMapper    objectMapper = new ObjectMapper();
    private final RestTemplate    restTemplate = new RestTemplate();

    // ── Token cache ───────────────────────────────────────────────────────────
    private String  spotifyToken     = null;
    private long    tokenExpiresAt   = 0L;

    // ── Discovery feed state ──────────────────────────────────────────────────
    private volatile boolean fetchRunning  = false;
    private volatile int     fetchProgress = 0;   // 0–100 percent
    private volatile String  fetchError    = null;
    private final List<SpotifySearchResult> lastResults = Collections.synchronizedList(new ArrayList<>());

    // ── Status ────────────────────────────────────────────────────────────────

    public Map<String, Object> getStatus() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("running",          fetchRunning);
        s.put("progress",         fetchProgress);
        s.put("resultCount",      lastResults.size());
        if (fetchError != null) s.put("error", fetchError);
        return s;
    }

    private List<String> getKnownArtistQueries() {
        List<Artist> artists = artistRepository.findAll();
        List<String> queries = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Artist a : artists) {
            if (a.getName() == null || a.getName().isBlank()) continue;
            if (sb.length() > 0) sb.append(" OR ");
            sb.append("artist:\"").append(a.getName()).append("\"");
            count++;
            if (count == 5) {
                queries.add(sb.toString());
                sb = new StringBuilder();
                count = 0;
            }
        }
        if (sb.length() > 0) {
            queries.add(sb.toString());
        }
        return queries;
    }

    public List<SpotifySearchResult> getLastResults() {
        return List.copyOf(lastResults);
    }

    // ── Discovery Feed Fetch ──────────────────────────────────────────────────

    /**
     * Starts an async discovery fetch across Spotify and YouTube.
     * Results replace the previous run and are available via {@link #getLastResults()}.
     */
    public void startDiscoveryFetch() {
        if (fetchRunning) {
            log.info("Discovery fetch already running, ignoring duplicate request");
            return;
        }
        fetchRunning  = true;
        fetchProgress = 0;
        fetchError    = null;
        lastResults.clear();

        Thread t = new Thread(() -> {
            try {
                List<SpotifySearchResult> combined = new ArrayList<>();

                // Collect all existing media URLs to mark already-imported items
                Set<String> importedUrls = trackRepository.findAll().stream()
                    .map(track -> track.getMediaUrl())
                    .collect(Collectors.toSet());

                // ── Phase 1: Spotify ──────────────────────────────────────────
                String token = getSpotifyToken();
                if (token != null) {
                    int total = SPOTIFY_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        String query = SPOTIFY_QUERIES.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchSpotify(token, query, importedUrls);
                            // Strict AI filter — only keep genuinely AI-generated tracks
                            List<SpotifySearchResult> aiOnly = hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .collect(Collectors.toList());
                            combined.addAll(aiOnly);
                            log.info("Spotify query '{}' → {} raw / {} passed AI filter",
                                query, hits.size(), aiOnly.size());
                        } catch (Exception e) {
                            log.warn("Spotify discovery query '{}' failed: {}", query, e.getMessage());
                        }
                        fetchProgress = (int) ((i + 1) * 30.0 / total);
                        Thread.sleep(200); // respect rate limits
                    }
                } else {
                    log.warn("AI Discovery: Spotify token unavailable — skipping Spotify phase");
                }

                // ── Phase 1.5: Known Artists on Spotify ──────────────────────
                if (token != null) {
                    List<String> knownQueries = getKnownArtistQueries();
                    for (int i = 0; i < knownQueries.size(); i++) {
                        String query = knownQueries.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchSpotify(token, query, importedUrls);
                            // Bypass strict AI filter for known artists
                            combined.addAll(hits);
                            log.info("Spotify known artist query '{}' → {} raw hits bypassed AI filter",
                                query, hits.size());
                        } catch (Exception e) {
                            log.warn("Spotify known artist query '{}' failed: {}", query, e.getMessage());
                        }
                        Thread.sleep(200); // respect rate limits
                    }
                }

                // ── Phase 2: YouTube ─────────────────────────────────────────
                if (youtubeApiKey != null && !youtubeApiKey.isBlank()) {
                    int total = YOUTUBE_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        String query = YOUTUBE_QUERIES.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchYouTube(query, importedUrls);
                            // Strict AI filter on YouTube results too
                            List<SpotifySearchResult> aiOnly = hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .collect(Collectors.toList());
                            combined.addAll(aiOnly);
                            log.info("YouTube query '{}' → {} raw / {} passed AI filter",
                                query, hits.size(), aiOnly.size());
                        } catch (Exception e) {
                            log.warn("YouTube discovery query '{}' failed: {}", query, e.getMessage());
                        }
                        fetchProgress = 30 + (int) ((i + 1) * 25.0 / total);
                        Thread.sleep(200);
                    }
                } else {
                    log.warn("AI Discovery: YouTube API key unavailable — skipping YouTube phase");
                }

                // ── Phase 3: SoundCloud ──────────────────────────────────────
                try {
                    int total = SOUNDCLOUD_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        String query = SOUNDCLOUD_QUERIES.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchSoundCloud(query, importedUrls);
                            List<SpotifySearchResult> aiOnly = hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .collect(Collectors.toList());
                            combined.addAll(aiOnly);
                            log.info("SoundCloud query '{}' → {} raw / {} passed AI filter",
                                query, hits.size(), aiOnly.size());
                        } catch (Exception e) {
                            log.warn("SoundCloud discovery query '{}' failed: {}", query, e.getMessage());
                        }
                        fetchProgress = 55 + (int) ((i + 1) * 15.0 / total);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.warn("SoundCloud discovery phase failed: {}", e.getMessage());
                }

                // ── Phase 4: Apple Music ─────────────────────────────────────
                try {
                    int total = ITUNES_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        String query = ITUNES_QUERIES.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchItunes(query, importedUrls);
                            List<SpotifySearchResult> aiOnly = hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .collect(Collectors.toList());
                            combined.addAll(aiOnly);
                            log.info("Apple Music query '{}' → {} raw / {} passed AI filter",
                                query, hits.size(), aiOnly.size());
                        } catch (Exception e) {
                            log.warn("Apple Music query '{}' failed: {}", query, e.getMessage());
                        }
                        fetchProgress = 70 + (int) ((i + 1) * 15.0 / total);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.warn("Apple Music discovery phase failed: {}", e.getMessage());
                }

                // ── Phase 5: Reddit (r/SunoAI, r/UdioMusic) ──────────────────
                try {
                    int total = REDDIT_SUBREDDITS.size();
                    for (int i = 0; i < total; i++) {
                        String sub = REDDIT_SUBREDDITS.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchReddit(sub, importedUrls);
                            combined.addAll(hits);
                            log.info("Reddit r/{} → {} AI tracks found", sub, hits.size());
                        } catch (Exception e) {
                            log.warn("Reddit discovery for r/{} failed: {}", sub, e.getMessage());
                        }
                        fetchProgress = 85 + (int) ((i + 1) * 10.0 / total);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.warn("Reddit discovery phase failed: {}", e.getMessage());
                }

                // ── Deduplicate & sort ────────────────────────────────────────
                Map<String, SpotifySearchResult> deduped = new LinkedHashMap<>();
                for (SpotifySearchResult r : combined) {
                    String key = r.getSpotifyUrl() != null && !r.getSpotifyUrl().isBlank()
                        ? r.getSpotifyUrl()
                        : (r.getTitle() + "|" + r.getArtist());
                    deduped.putIfAbsent(key, r);
                }

                // Sort: filter English only, not-yet-imported first, then by release date desc, then popularity desc
                List<SpotifySearchResult> sorted = deduped.values().stream()
                    .filter(r -> languageDetectionService.isLikelyEnglish(r.getTitle()))
                    .sorted(Comparator
                        .comparing(SpotifySearchResult::isAlreadyImported)
                        .thenComparing(Comparator.<SpotifySearchResult, String>comparing(
                            r -> r.getReleaseDate() != null ? r.getReleaseDate() : "").reversed())
                        .thenComparing(Comparator.comparingInt(SpotifySearchResult::getPopularity).reversed()))
                    .collect(Collectors.toList());

                lastResults.clear();
                lastResults.addAll(sorted);
                fetchProgress = 100;
                log.info("AI Discovery fetch complete: {} unique results", sorted.size());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fetchError = "Fetch interrupted";
            } catch (Exception e) {
                fetchError = e.getMessage();
                log.error("AI Discovery fetch error: {}", e.getMessage());
            } finally {
                fetchRunning = false;
            }
        });
        t.setDaemon(true);
        t.setName("ai-discovery-fetch");
        t.start();
    }

    /**
     * Same as {@link #startDiscoveryFetch()} but automatically imports every
     * discovered track that passes the AI filter and is not already in the library.
     * Called by the scheduled job so the library grows without manual admin effort.
     *
     * <p>Spotify tracks must have popularity >= 10 to avoid test/throwaway uploads.
     * YouTube tracks are imported regardless of popularity (score not available at scan time).
     */
    public void startDiscoveryFetchWithAutoImport() {
        if (fetchRunning) {
            log.info("Discovery fetch already running — skipping auto-import trigger");
            return;
        }
        fetchRunning  = true;
        fetchProgress = 0;
        fetchError    = null;
        lastResults.clear();

        Thread t = new Thread(() -> {
            try {
                List<SpotifySearchResult> combined = new ArrayList<>();
                Set<String> importedUrls = trackRepository.findAll().stream()
                    .map(track -> track.getMediaUrl())
                    .collect(Collectors.toSet());

                String token = getSpotifyToken();
                if (token != null) {
                    int total = SPOTIFY_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        try {
                            List<SpotifySearchResult> hits = searchSpotify(token, SPOTIFY_QUERIES.get(i), importedUrls);
                            hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .forEach(combined::add);
                        } catch (Exception e) {
                            log.warn("Auto-import Spotify query failed: {}", e.getMessage());
                        }
                        fetchProgress = (int) ((i + 1) * 40.0 / total);
                        Thread.sleep(200);
                    }

                    // ── Phase 1.5: Known Artists on Spotify (Auto-Import) ────
                    List<String> knownQueries = getKnownArtistQueries();
                    for (int i = 0; i < knownQueries.size(); i++) {
                        try {
                            List<SpotifySearchResult> hits = searchSpotify(token, knownQueries.get(i), importedUrls);
                            // Bypass strict AI filter for known artists
                            combined.addAll(hits);
                        } catch (Exception e) {
                            log.warn("Auto-import Spotify known artist query failed: {}", e.getMessage());
                        }
                        Thread.sleep(200);
                    }
                }

                if (youtubeApiKey != null && !youtubeApiKey.isBlank()) {
                    int total = YOUTUBE_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        try {
                            List<SpotifySearchResult> hits = searchYouTube(YOUTUBE_QUERIES.get(i), importedUrls);
                            hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .forEach(combined::add);
                        } catch (Exception e) {
                            log.warn("Auto-import YouTube query failed: {}", e.getMessage());
                        }
                        fetchProgress = 30 + (int) ((i + 1) * 25.0 / total);
                        Thread.sleep(200);
                    }
                }

                try {
                    int total = SOUNDCLOUD_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        try {
                            List<SpotifySearchResult> hits = searchSoundCloud(SOUNDCLOUD_QUERIES.get(i), importedUrls);
                            hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .forEach(combined::add);
                        } catch (Exception e) {
                            log.warn("Auto-import SoundCloud query failed: {}", e.getMessage());
                        }
                        fetchProgress = 55 + (int) ((i + 1) * 15.0 / total);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.warn("Auto-import SoundCloud phase failed: {}", e.getMessage());
                }

                try {
                    int total = ITUNES_QUERIES.size();
                    for (int i = 0; i < total; i++) {
                        try {
                            List<SpotifySearchResult> hits = searchItunes(ITUNES_QUERIES.get(i), importedUrls);
                            hits.stream()
                                .filter(r -> isLikelyAiGenerated(r.getArtist(), r.getTitle()))
                                .forEach(combined::add);
                        } catch (Exception e) {
                            log.warn("Auto-import Apple Music query failed: {}", e.getMessage());
                        }
                        fetchProgress = 70 + (int) ((i + 1) * 15.0 / total);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.warn("Auto-import Apple Music phase failed: {}", e.getMessage());
                }

                try {
                    int total = REDDIT_SUBREDDITS.size();
                    for (int i = 0; i < total; i++) {
                        String sub = REDDIT_SUBREDDITS.get(i);
                        try {
                            List<SpotifySearchResult> hits = searchReddit(sub, importedUrls);
                            combined.addAll(hits);
                        } catch (Exception e) {
                            log.warn("Auto-import Reddit r/{} failed: {}", sub, e.getMessage());
                        }
                        fetchProgress = 85 + (int) ((i + 1) * 10.0 / total);
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.warn("Auto-import Reddit phase failed: {}", e.getMessage());
                }

                // Deduplicate and apply Language Filter
                Map<String, SpotifySearchResult> deduped = new LinkedHashMap<>();
                for (SpotifySearchResult r : combined) {
                    if (!languageDetectionService.isLikelyEnglish(r.getTitle())) {
                        continue;
                    }
                    String key = r.getSpotifyUrl() != null && !r.getSpotifyUrl().isBlank()
                        ? r.getSpotifyUrl() : (r.getTitle() + "|" + r.getArtist());
                    deduped.putIfAbsent(key, r);
                }

                // Auto-import new tracks
                int imported = 0;
                int skipped  = 0;
                List<SpotifySearchResult> candidates = new ArrayList<>(deduped.values());
                for (int i = 0; i < candidates.size(); i++) {
                    SpotifySearchResult r = candidates.get(i);
                    if (r.isAlreadyImported()) { skipped++; continue; }
                    // Spotify: require popularity >= 10 to avoid junk uploads
                    if ("Spotify".equals(r.getPlatformSource()) && r.getPopularity() < 10) { skipped++; continue; }
                    try {
                        autoImport(r);
                        r.setAlreadyImported(true);
                        imported++;
                    } catch (DataIntegrityViolationException e) {
                        skipped++; // already in DB
                    } catch (Exception e) {
                        log.warn("Auto-import failed for '{}': {}", r.getTitle(), e.getMessage());
                        skipped++;
                    }
                    fetchProgress = 80 + (int) ((i + 1) * 20.0 / candidates.size());
                }

                lastResults.clear();
                lastResults.addAll(candidates);
                fetchProgress = 100;
                log.info("AI Discovery auto-import complete: {} imported, {} skipped/already-present",
                         imported, skipped);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fetchError = "Auto-import interrupted";
            } catch (Exception e) {
                fetchError = e.getMessage();
                log.error("AI Discovery auto-import error: {}", e.getMessage());
            } finally {
                fetchRunning = false;
            }
        });
        t.setDaemon(true);
        t.setName("ai-discovery-auto-import");
        t.start();
    }

    /** Creates a Track and auto-creates the artist if they don't already exist. */
    private void autoImport(SpotifySearchResult r) {
        // Auto-create artist
        if (r.getArtist() != null && !r.getArtist().isBlank()) {
            for (String raw : r.getArtist().split(",")) {
                String name = raw.trim();
                if (name.isBlank()) continue;
                boolean exists = artistRepository.findByNameContainingIgnoreCase(name)
                    .stream().anyMatch(a -> a.getName().equalsIgnoreCase(name));
                if (!exists) {
                    Artist artist = new Artist();
                    artist.setName(name);
                    artist.setAiToolsUsed("AI Discovery");
                    if (r.getAlbumImageUrl() != null && !r.getAlbumImageUrl().isBlank())
                        artist.setImageUrl(r.getAlbumImageUrl());
                    artistRepository.save(artist);
                }
            }
        }
        TrackDTO dto = new TrackDTO();
        dto.setTitle(r.getTitle());
        dto.setCreator(r.getArtist());
        dto.setMediaUrl(r.getSpotifyUrl());
        dto.setPlatformSource(r.getPlatformSource());
        dto.setEmbedUrl(r.getEmbedUrl());
        dto.setImageUrl(r.getAlbumImageUrl());
        dto.setAiToolsUsed("");
        dto.setGenre("");
        dto.setFeaturedStatus(false);
        dto.setSpotifyPopularity(r.getPopularity());
        dto.setReleaseDate(r.getReleaseDate());
        trackService.createTrack(dto);
    }

    // ── Spotify ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private synchronized String getSpotifyToken() {
        if (spotifyClientId == null || spotifyClientId.isBlank()) return null;
        if (spotifyToken != null && System.currentTimeMillis() < tokenExpiresAt) return spotifyToken;
        try {
            String creds = Base64.getEncoder()
                .encodeToString((spotifyClientId + ":" + spotifyClientSecret).getBytes());
            HttpHeaders h = new HttpHeaders();
            h.set("Authorization", "Basic " + creds);
            h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> req = new HttpEntity<>("grant_type=client_credentials", h);
            ResponseEntity<String> resp = restTemplate.postForEntity(SPOTIFY_TOKEN_URL, req, String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());
            spotifyToken   = root.get("access_token").asText();
            tokenExpiresAt = System.currentTimeMillis() + (root.get("expires_in").asLong(3600) - 120) * 1000;
            return spotifyToken;
        } catch (Exception e) {
            log.warn("Could not obtain Spotify token for AI Discovery: {}", e.getMessage());
            return null;
        }
    }

    private List<SpotifySearchResult> searchSpotify(String token, String query,
                                                      Set<String> importedUrls) throws Exception {
        String url = SPOTIFY_SEARCH_URL + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                     + "&type=track&limit=50&market=US";
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        HttpEntity<Void> req = new HttpEntity<>(h);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
        JsonNode root   = objectMapper.readTree(resp.getBody());
        JsonNode items  = root.path("tracks").path("items");
        List<SpotifySearchResult> results = new ArrayList<>();
        for (JsonNode item : items) {
            try {
                String id          = item.path("id").asText("");
                String title       = item.path("name").asText("");
                String releaseDate = item.path("album").path("release_date").asText("");
                int popularity     = item.path("popularity").asInt(0);
                String spotifyUrl  = item.path("external_urls").path("spotify").asText("");
                String embedUrl    = id.isBlank() ? "" : "https://open.spotify.com/embed/track/" + id;
                String imageUrl    = "";
                JsonNode images    = item.path("album").path("images");
                if (images.isArray() && images.size() > 0) imageUrl = images.get(0).path("url").asText("");
                String artist = "";
                JsonNode artists = item.path("artists");
                if (artists.isArray()) {
                    List<String> names = new ArrayList<>();
                    artists.forEach(a -> names.add(a.path("name").asText("")));
                    artist = String.join(", ", names);
                }

                SpotifySearchResult r = new SpotifySearchResult(
                    id, title, artist,
                    item.path("album").path("name").asText(""),
                    imageUrl, spotifyUrl, embedUrl,
                    item.path("duration_ms").asInt(0), popularity
                );
                r.setReleaseDate(releaseDate);
                r.setAlreadyImported(importedUrls.contains(spotifyUrl));
                r.setPlatformSource("Spotify");
                results.add(r);
            } catch (Exception e) {
                log.trace("Skipping Spotify item: {}", e.getMessage());
            }
        }
        return results;
    }

    // ── YouTube ───────────────────────────────────────────────────────────────

    private List<SpotifySearchResult> searchYouTube(String query, Set<String> importedUrls) throws Exception {
        String url = YT_SEARCH_URL
            + "?part=snippet&type=video&videoCategoryId=10" // Music category
            + "&order=date"
            + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
            + "&maxResults=50"
            + "&key=" + youtubeApiKey;
        HttpHeaders h = new HttpHeaders();
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> req = new HttpEntity<>(h);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
        JsonNode root  = objectMapper.readTree(resp.getBody());
        JsonNode items = root.path("items");
        List<SpotifySearchResult> results = new ArrayList<>();
        for (JsonNode item : items) {
            try {
                String videoId    = item.path("id").path("videoId").asText("");
                if (videoId.isBlank()) continue;
                String title      = item.path("snippet").path("title").asText("");
                String channel    = item.path("snippet").path("channelTitle").asText("");
                String published  = item.path("snippet").path("publishedAt").asText("");
                String thumbUrl   = item.path("snippet").path("thumbnails").path("high").path("url").asText("");
                String watchUrl   = "https://www.youtube.com/watch?v=" + videoId;
                String embedUrl   = "https://www.youtube.com/embed/" + videoId;
                // Use release date as just the date portion of the ISO timestamp
                String releaseDate = published.length() >= 10 ? published.substring(0, 10) : published;

                SpotifySearchResult r = new SpotifySearchResult(
                    "yt-" + videoId, title, channel, "", thumbUrl,
                    watchUrl, embedUrl, 0, 0
                );
                r.setReleaseDate(releaseDate);
                r.setPublishedAt(published);
                r.setAlreadyImported(importedUrls.contains(watchUrl) || importedUrls.contains(embedUrl));
                r.setPlatformSource("YouTube");
                results.add(r);
            } catch (Exception e) {
                log.trace("Skipping YouTube item: {}", e.getMessage());
            }
        }
        return results;
    }

    // ── SoundCloud ────────────────────────────────────────────────────────────

    private List<SpotifySearchResult> searchSoundCloud(String query, Set<String> importedUrls) {
        if (soundCloudService == null || !soundCloudService.isConfigured()) {
            return List.of();
        }
        try {
            SpotifySearchPage page = soundCloudService.search(query, 25);
            List<SpotifySearchResult> results = page.getItems();
            for (SpotifySearchResult r : results) {
                r.setPlatformSource("SoundCloud");
                if (importedUrls.contains(r.getSpotifyUrl()) || importedUrls.contains(r.getEmbedUrl())) {
                    r.setAlreadyImported(true);
                }
            }
            return results;
        } catch (Exception e) {
            log.trace("SoundCloud search error: {}", e.getMessage());
            return List.of();
        }
    }

    // ── Apple Music (iTunes Search API) ───────────────────────────────────────

    private List<SpotifySearchResult> searchItunes(String query, Set<String> importedUrls) {
        List<SpotifySearchResult> results = new ArrayList<>();
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://itunes.apple.com/search")
                .queryParam("term", query)
                .queryParam("entity", "song")
                .queryParam("limit", 25)
                .toUriString();
            String json = restTemplate.getForObject(url, String.class);
            if (json == null) return results;
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("results");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = item.path("trackName").asText("");
                    String artist = item.path("artistName").asText("");
                    if (title.isBlank() || artist.isBlank()) continue;
                    String artwork = item.path("artworkUrl100").asText("").replace("100x100bb", "600x600bb");
                    String previewUrl = item.path("previewUrl").asText("");
                    String trackViewUrl = item.path("trackViewUrl").asText("");
                    String releaseDate = item.path("releaseDate").asText("");
                    if (releaseDate.length() > 10) releaseDate = releaseDate.substring(0, 10);

                    SpotifySearchResult r = new SpotifySearchResult(
                        "apple-" + item.path("trackId").asText(),
                        title, artist, item.path("collectionName").asText("Single"),
                        artwork, trackViewUrl, previewUrl,
                        item.path("trackTimeMillis").asInt(180000), 70
                    );
                    r.setReleaseDate(releaseDate);
                    r.setPlatformSource("Apple Music");
                    if (importedUrls.contains(trackViewUrl) || importedUrls.contains(previewUrl)) {
                        r.setAlreadyImported(true);
                    }
                    results.add(r);
                }
            }
        } catch (Exception e) {
            log.trace("iTunes search error: {}", e.getMessage());
        }
        return results;
    }

    // ── Reddit (r/SunoAI, r/UdioMusic, r/AIMusicCreate) ───────────────────────

    private List<SpotifySearchResult> searchReddit(String subreddit, Set<String> importedUrls) {
        List<SpotifySearchResult> results = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MiksterMediaBot/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = "https://www.reddit.com/r/" + subreddit + "/new.json?limit=25";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) return results;
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode children = root.path("data").path("children");
            if (children.isArray()) {
                for (JsonNode child : children) {
                    JsonNode data = child.path("data");
                    String postTitle = data.path("title").asText("");
                    String author = data.path("author").asText("");
                    String urlStr = data.path("url").asText("");
                    String selftext = data.path("selftext").asText("");
                    long createdUtc = data.path("created_utc").asLong(0);
                    int score = data.path("score").asInt(1);

                    String mediaUrl = extractMediaUrl(urlStr);
                    if (mediaUrl == null) {
                        mediaUrl = extractMediaUrl(selftext);
                    }
                    if (mediaUrl != null && !postTitle.isBlank()) {
                        SpotifySearchResult r = new SpotifySearchResult(
                            "reddit-" + data.path("id").asText(),
                            postTitle.trim(),
                            author + " (r/" + subreddit + ")",
                            "r/" + subreddit + " Community",
                            "", mediaUrl, mediaUrl,
                            180000, Math.min(100, Math.max(20, score * 5))
                        );
                        if (createdUtc > 0) {
                            r.setReleaseDate(java.time.Instant.ofEpochSecond(createdUtc)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate().toString());
                        } else {
                            r.setReleaseDate(java.time.LocalDate.now().toString());
                        }
                        r.setPlatformSource("Reddit");
                        if (importedUrls.contains(mediaUrl)) {
                            r.setAlreadyImported(true);
                        }
                        results.add(r);
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Reddit search error for r/{}: {}", subreddit, e.getMessage());
        }
        return results;
    }

    private String extractMediaUrl(String text) {
        if (text == null || text.isBlank()) return null;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "https?://(?:suno\\.com/song/|www\\.udio\\.com/songs/|youtu\\.be/|www\\.youtube\\.com/watch|soundcloud\\.com/)[^\\s)\\]\"'>]+"
        );
        java.util.regex.Matcher m = pattern.matcher(text);
        if (m.find()) {
            return m.group(0);
        }
        return null;
    }

    // ── AI generation filter ───────────────────────────────────────────────────

    /**
     * Returns {@code true} only if the track is very likely to be AI-generated.
     *
     * <p>Checks are case-insensitive substring matches against two lists:
     * <ul>
     *   <li>{@code AI_ARTIST_TERMS} — artist name contains a known AI tool/platform</li>
     *   <li>{@code AI_TITLE_TERMS}  — track title contains a specific AI phrase</li>
     * </ul>
     *
     * <p>A match on EITHER list is sufficient to pass, but single-word tokens like
     * bare "ai" are intentionally absent from the title list to avoid false positives
     * (e.g. "Jai" or "Taiwan" both contain the letters "ai").
     */
    private boolean isLikelyAiGenerated(String artist, String title) {
        String artistLower = artist  != null ? artist.toLowerCase()  : "";
        String titleLower  = title   != null ? title.toLowerCase()   : "";

        // Check artist name against known AI tool names
        for (String term : AI_ARTIST_TERMS) {
            if (artistLower.contains(term)) {
                log.debug("AI filter PASS (artist match '{}') — {}", term, artist);
                return true;
            }
        }

        // Check title against specific multi-word AI phrases
        for (String term : AI_TITLE_TERMS) {
            if (titleLower.contains(term)) {
                log.debug("AI filter PASS (title match '{}') — {}", term, title);
                return true;
            }
        }

        log.debug("AI filter REJECT — artist='{}', title='{}'", artist, title);
        return false;
    }
}
