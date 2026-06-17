package com.mikstermedia.service;

import com.mikstermedia.dto.SpotifySearchResult;
import com.mikstermedia.dto.SpotifySearchPage;
import com.mikstermedia.model.Artist;
import com.mikstermedia.model.PlatformSetting;
import com.mikstermedia.model.Track;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.PlatformSettingRepository;
import com.mikstermedia.repository.TrackRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spotify Web API client — supports both Client Credentials (for search)
 * and Authorization Code OAuth flow (for playlists, top-tracks, recommendations).
 *
 * <p>Setup:
 * <ol>
 *   <li>Set {@code spotify.client-id} and {@code spotify.client-secret} in application.properties</li>
 *   <li>Add {@code http://localhost:8081/api/spotify/auth/callback} as a Redirect URI
 *       in your Spotify Dashboard app settings</li>
 *   <li>Admin visits /admin/spotify-discovery and clicks "Connect Spotify"</li>
 * </ol>
 */
@Service
@Slf4j
public class SpotifyService {

    private static final String TOKEN_URL   = "https://accounts.spotify.com/api/token";
    private static final String SEARCH_URL  = "https://api.spotify.com/v1/search";
    private static final String AUTH_URL    = "https://accounts.spotify.com/authorize";
    private static final String SCOPES      =
        "playlist-read-private playlist-read-collaborative user-top-read";

    @Value("${spotify.client-id:}")
    private String clientId;

    @Value("${spotify.client-secret:}")
    private String clientSecret;

    @Value("${spotify.redirect-uri:https://localhost:8081/api/spotify/auth/callback}")
    private String redirectUri;

    private static final String TRACKS_URL   = "https://api.spotify.com/v1/tracks/";
    private static final String ARTISTS_URL  = "https://api.spotify.com/v1/artists/";
    private static final Pattern TRACK_ID_RE  =
        Pattern.compile("spotify\\.com(?:/embed)?/track/([a-zA-Z0-9]+)");
    private static final Pattern ARTIST_ID_RE =
        Pattern.compile("spotify\\.com/artist/([a-zA-Z0-9]+)");

    private final TrackRepository  trackRepository;
    private final ArtistRepository artistRepository;
    private final RestClient       restClient;

    // ── Client Credentials token (for search) ────────────────────────────────
    private String  cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    // ── Authorization Code tokens (for playlists / trending) ─────────────────
    private volatile String  userAccessToken;
    private volatile String  userRefreshToken;
    private volatile Instant userTokenExpiry  = Instant.EPOCH;
    private volatile String  pendingOAuthState; // CSRF guard

    private final LastFmService lastFmService;
    private final YouTubeService youTubeService;
    private final WeeklyChartService weeklyChartService;
    private final PlatformSettingRepository settingRepo;

    /** DB key used to persist the Spotify user refresh token across restarts. */
    private static final String SETTING_REFRESH_TOKEN = "spotify_refresh_token";

    // ── Async State ───────────────────────────────────────────────────────────
    private volatile boolean isTracksRefreshing = false;
    private volatile int tracksRefreshTotal = 0;
    private volatile int tracksRefreshCompleted = 0;
    private volatile int tracksRefreshErrors = 0;

    private volatile boolean isArtistsRefreshing = false;
    private volatile int artistsRefreshTotal = 0;
    private volatile int artistsRefreshCompleted = 0;
    private volatile int artistsRefreshErrors = 0;

    // ── User OAuth State ──────────────────────────────────────────────────────
    private final SunoScraperService sunoScraperService;
    private final UdioScraperService udioScraperService;

    public SpotifyService(TrackRepository trackRepository, ArtistRepository artistRepository, 
                          LastFmService lastFmService, YouTubeService youTubeService, 
                          WeeklyChartService weeklyChartService, SunoScraperService sunoScraperService, 
                          UdioScraperService udioScraperService,
                          PlatformSettingRepository settingRepo) {
        this.trackRepository  = trackRepository;
        this.artistRepository = artistRepository;
        this.lastFmService    = lastFmService;
        this.youTubeService   = youTubeService;
        this.weeklyChartService = weeklyChartService;
        this.sunoScraperService = sunoScraperService;
        this.udioScraperService = udioScraperService;
        this.settingRepo      = settingRepo;
        this.restClient = RestClient.create();
    }

    // ── Startup: restore persisted token ─────────────────────────────────────

    /**
     * On application startup, loads a previously-persisted Spotify refresh token
     * from the database and silently obtains a fresh access token so the admin
     * never has to re-authenticate after a backend restart.
     */
    @PostConstruct
    public void initFromDb() {
        settingRepo.findById(SETTING_REFRESH_TOKEN).ifPresent(s -> {
            String saved = s.getSettingValue();
            if (saved != null && !saved.isBlank()) {
                userRefreshToken = saved;
                log.info("Spotify: loaded persisted refresh token from DB — silently refreshing access token");
                try {
                    refreshUserToken();
                } catch (Exception e) {
                    log.warn("Spotify: initial token refresh failed ({}); admin may need to re-connect", e.getMessage());
                }
            }
        });
    }

    // ── Scheduled keep-alive ──────────────────────────────────────────────────

    /**
     * Proactively refreshes the Spotify user access token every 45 minutes
     * so it never expires between admin sessions.
     * Only runs if a refresh token is already present.
     */
    @Scheduled(fixedDelay = 45 * 60 * 1000)   // every 45 minutes
    public void scheduledTokenRefresh() {
        if (userRefreshToken == null || userRefreshToken.isBlank()) return;
        log.debug("Spotify scheduled keep-alive: refreshing user token");
        try {
            refreshUserToken();
        } catch (Exception e) {
            log.warn("Spotify scheduled token refresh failed: {}", e.getMessage());
        }
    }

    // ── Token persistence helpers ─────────────────────────────────────────────

    /** Saves (or updates) the refresh token in the platform_settings table. */
    private void persistRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        PlatformSetting row = settingRepo.findById(SETTING_REFRESH_TOKEN)
            .orElse(new PlatformSetting(SETTING_REFRESH_TOKEN, refreshToken));
        row.setSettingValue(refreshToken);
        settingRepo.save(row);
        log.debug("Spotify: refresh token persisted to DB");
    }

    /** Returns true if Spotify credentials have been configured. */
    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
            && clientSecret != null && !clientSecret.isBlank();
    }

    /** Returns true if an admin has completed the Spotify OAuth flow. */
    public boolean isUserAuthenticated() {
        return userRefreshToken != null && !userRefreshToken.isBlank();
    }

    /**
     * Builds the Spotify Authorization URL for the admin OAuth flow.
     * Stores a random state token for CSRF validation.
     */
    public String buildAuthorizationUrl() {
        pendingOAuthState = java.util.UUID.randomUUID().toString();
        try {
            return AUTH_URL
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + java.net.URLEncoder.encode(redirectUri,
                    java.nio.charset.StandardCharsets.UTF_8)
                + "&scope=" + java.net.URLEncoder.encode(SCOPES,
                    java.nio.charset.StandardCharsets.UTF_8)
                + "&state=" + pendingOAuthState
                + "&show_dialog=false";
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Spotify auth URL", e);
        }
    }

    /**
     * Exchanges the OAuth authorization code (from Spotify's callback) for
     * access + refresh tokens and stores them in memory.
     */
    @SuppressWarnings("unchecked")
    public synchronized void exchangeCodeForTokens(String code) {
        try {
            String credentials = java.util.Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type",   "authorization_code");
            form.add("code",         code);
            form.add("redirect_uri", redirectUri);

            Map<?, ?> resp = restClient.post()
                .uri(TOKEN_URL)
                .header("Authorization", "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

            if (resp == null) throw new RuntimeException("Null token response");

            userAccessToken  = (String) resp.get("access_token");
            userRefreshToken = (String) resp.get("refresh_token");
            int expiresIn    = resp.get("expires_in") instanceof Number n ? n.intValue() : 3600;
            userTokenExpiry  = Instant.now().plusSeconds(expiresIn - 120);

            // Persist the refresh token so it survives backend restarts
            persistRefreshToken(userRefreshToken);

            log.info("Spotify user token obtained successfully");
        } catch (Exception e) {
            log.error("Failed to exchange Spotify auth code: {}", e.getMessage());
            throw new RuntimeException("Token exchange failed: " + e.getMessage());
        }
    }

    /** Returns a valid user-level access token, refreshing if needed. */
    public synchronized String getUserToken() {
        if (userAccessToken != null && Instant.now().isBefore(userTokenExpiry)) {
            return userAccessToken;
        }
        if (userRefreshToken != null) {
            return refreshUserToken();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private synchronized String refreshUserToken() {
        try {
            String credentials = java.util.Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type",    "refresh_token");
            form.add("refresh_token", userRefreshToken);

            Map<?, ?> resp = restClient.post()
                .uri(TOKEN_URL)
                .header("Authorization", "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

            if (resp == null) return null;

            userAccessToken = (String) resp.get("access_token");
            // Spotify may rotate the refresh token — persist whatever we get
            if (resp.get("refresh_token") instanceof String rt) {
                userRefreshToken = rt;
                persistRefreshToken(rt);
            }
            int expiresIn   = resp.get("expires_in") instanceof Number n ? n.intValue() : 3600;
            userTokenExpiry = Instant.now().plusSeconds(expiresIn - 120);

            log.info("Spotify user token refreshed successfully");
            return userAccessToken;
        } catch (Exception e) {
            log.error("Failed to refresh user Spotify token: {}", e.getMessage());
            userAccessToken  = null;
            userRefreshToken = null;
            userTokenExpiry  = Instant.EPOCH;
            return null;
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Searches Spotify for tracks matching {@code query}.
     *
     * @param query  free-text search (e.g. "suno ai", "udio generated")
     * @param limit  max results per page (1–10 in Developer Mode)
     * @param offset zero-based page offset for pagination
     * @return list of {@link SpotifySearchResult}, each flagged if already imported
     */
    @SuppressWarnings("unchecked")
    public SpotifySearchPage search(String query, int limit, int offset) {
        if (!isConfigured()) {
            log.warn("Spotify credentials not configured — returning empty results");
            return new SpotifySearchPage(List.of(), 0, 0, limit);
        }

        String token = getAccessToken();
        if (token == null) return new SpotifySearchPage(List.of(), 0, 0, limit);

        // Spotify Developer Mode apps are capped at 10 results per request.
        // To increase this, request Extended Quota Mode at developer.spotify.com.
        int safeLimit = Math.max(1, Math.min(10, limit));
        if (limit > 10) {
            log.warn("Spotify limit clamped from {} to 10 (Developer Mode restriction)", limit);
        }

        int safeOffset = Math.max(0, offset);

        try {
            Map<?, ?> response = restClient.get()
                .uri(SEARCH_URL + "?q={q}&type=track&limit={l}&offset={o}", query, safeLimit, safeOffset)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);

            return parseSearchResponse(response, safeLimit, safeOffset);
        } catch (Exception e) {
            log.error("Spotify search failed: {}", e.getMessage());
            return new SpotifySearchPage(List.of(), 0, safeOffset, safeLimit);
        }
    }

    /** Playlist search terms targeting curated AI-music collections. */
    private static final List<String> PLAYLIST_QUERIES = List.of(
        "AI music 2025",
        "suno ai",
        "udio ai",
        "AI generated music",
        "artificial intelligence music"
    );

    private static final String PLAYLIST_SEARCH_URL = "https://api.spotify.com/v1/search";
    private static final String PLAYLIST_TRACKS_URL  = "https://api.spotify.com/v1/playlists/{id}/tracks";

    /**
     * Fetches trending AI music by:
     * <ol>
     *   <li>Searching Spotify for AI-music playlists (5 queries × 3 playlists = up to 15 playlists)</li>
     *   <li>Pulling the top 10 tracks from each playlist</li>
     *   <li>Deduplicating by Spotify track ID</li>
     *   <li>Sorting by {@code popularity} descending</li>
     *   <li>Returning the top {@code topN}</li>
     * </ol>
     *
     * <p>Playlist tracks have genuine popularity scores because real listeners
     * have curated and streamed them, unlike keyword-matched track searches.
     *
     * @param topN how many top results to return
     */
    @SuppressWarnings("unchecked")
    public List<SpotifySearchResult> trending(int topN) {
        if (!isConfigured()) return List.of();

        String token = getAccessToken();
        if (token == null) return List.of();

        java.util.Set<String> existingUrls = new java.util.HashSet<>(
            trackRepository.findAll().stream().map(t -> t.getMediaUrl()).toList()
        );

        // Step 1: collect playlist IDs from AI music search queries
        java.util.LinkedHashSet<String> playlistIds = new java.util.LinkedHashSet<>();
        for (String query : PLAYLIST_QUERIES) {
            try {
                Map<?, ?> resp = restClient.get()
                    .uri(PLAYLIST_SEARCH_URL + "?q={q}&type=playlist&limit=3", query)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

                Map<?, ?> playlistsObj = resp == null ? null : (Map<?, ?>) resp.get("playlists");
                if (playlistsObj == null) continue;
                List<?> items = (List<?>) playlistsObj.get("items");
                if (items == null) continue;

                for (Object item : items) {
                    Map<?, ?> pl = (Map<?, ?>) item;
                    if (pl == null) continue;
                    String id = str(pl, "id");
                    if (!id.isBlank()) playlistIds.add(id);
                }
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Playlist search for '{}' failed: {}", query, e.getMessage());
            }
        }

        log.info("Trending: found {} AI playlists to sample", playlistIds.size());

        // Step 2: pull tracks from each playlist
        // Requires user-level OAuth token (Spotify blocks this for Client Credentials)
        String userToken = getUserToken();
        if (userToken == null) {
            log.warn("Trending: no user OAuth token — cannot fetch playlist tracks. " +
                     "Admin must connect Spotify via /api/spotify/auth/login.");
            return List.of();
        }

        java.util.Map<String, SpotifySearchResult> seen = new java.util.LinkedHashMap<>();
        for (String playlistId : playlistIds) {
            try {
                Map<?, ?> resp = restClient.get()
                    .uri(PLAYLIST_TRACKS_URL + "?limit=10&fields=items(track(id,name,popularity,artists,album,external_urls,duration_ms))",
                         playlistId)
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(Map.class);

                if (resp == null) continue;
                List<?> items = (List<?>) resp.get("items");
                if (items == null) continue;

                for (Object item : items) {
                    try {
                        Map<?, ?> wrapper = (Map<?, ?>) item;
                        if (wrapper == null) continue;
                        Map<?, ?> track = (Map<?, ?>) wrapper.get("track");
                        if (track == null) continue;

                        String id      = str(track, "id");
                        String name    = str(track, "name");
                        int duration   = num(track, "duration_ms");
                        int popularity = num(track, "popularity");

                        List<?> artistsList = (List<?>) track.get("artists");
                        String artist = "";
                        String primaryArtistId = "";
                        if (artistsList != null && !artistsList.isEmpty()) {
                            Map<?, ?> primaryArtistMap = (Map<?, ?>) artistsList.get(0);
                            primaryArtistId = str(primaryArtistMap, "id");
                            artist = artistsList.stream()
                                .map(a -> str((Map<?,?>) a, "name"))
                                .filter(s -> !s.isBlank())
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("");
                        }

                        String album = "", imageUrl = "", releaseDate = "";
                        Map<?, ?> albumObj = (Map<?, ?>) track.get("album");
                        if (albumObj != null) {
                            album       = str(albumObj, "name");
                            releaseDate = str(albumObj, "release_date");
                            List<?> images = (List<?>) albumObj.get("images");
                            if (images != null && !images.isEmpty()) {
                                imageUrl = str((Map<?,?>) images.get(0), "url");
                            }
                        }

                        String spotifyUrl = "";
                        Map<?, ?> external = (Map<?, ?>) track.get("external_urls");
                        if (external != null) spotifyUrl = str(external, "spotify");

                        String embedUrl = id.isBlank() ? "" : "https://open.spotify.com/embed/track/" + id;

                        SpotifySearchResult result = new SpotifySearchResult(
                            id, name, artist, album, imageUrl,
                            spotifyUrl, embedUrl, duration, popularity
                        );
                        result.setReleaseDate(releaseDate);
                        result.setPrimaryArtistId(primaryArtistId);
                        result.setAlreadyImported(existingUrls.contains(spotifyUrl));
                        seen.putIfAbsent(id, result);  // deduplicate by track ID

                    } catch (Exception e) {
                        log.warn("Skipping playlist track: {}", e.getMessage());
                    }
                }

                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Playlist tracks fetch for {} failed: {}", playlistId, e.getMessage());
            }
        }

        log.info("Trending: {} unique tracks before sort, returning top {}", seen.size(), topN);

        // Step 3: sort by popularity desc, return top N
        return seen.values().stream()
            .filter(r -> !r.getSpotifyId().isBlank())
            .sorted(java.util.Comparator.comparingInt(SpotifySearchResult::getPopularity).reversed())
            .limit(Math.max(1, topN))
            .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Returns a valid access token, refreshing from Spotify if expired. */
    @SuppressWarnings("unchecked")
    private synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        // Clear stale state before attempting refresh
        cachedToken = null;
        tokenExpiry = Instant.EPOCH;

        try {
            // Spotify recommends Basic auth: Base64(client_id:client_secret)
            String credentials = java.util.Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");

            Map<String, Object> tokenResponse = restClient.post()
                .uri(TOKEN_URL)
                .header("Authorization", "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                log.error("Spotify token response missing access_token: {}", tokenResponse);
                return null;
            }

            cachedToken  = (String) tokenResponse.get("access_token");
            int expiresIn = tokenResponse.get("expires_in") instanceof Number n
                            ? n.intValue() : 3600;
            tokenExpiry  = Instant.now().plusSeconds(expiresIn - 120); // 2-min buffer

            log.info("Spotify token refreshed, valid for {}s (until {})", expiresIn, tokenExpiry);
            return cachedToken;

        } catch (Exception e) {
            log.error("Failed to obtain Spotify access token: {} — {}", e.getClass().getSimpleName(), e.getMessage());
            cachedToken = null;   // ensure we retry on next call
            tokenExpiry = Instant.EPOCH;
            return null;
        }
    }

    /** Parses the Spotify search JSON response into a paginated result page. */
    @SuppressWarnings("unchecked")
    private SpotifySearchPage parseSearchResponse(Map<?, ?> response, int limit, int offset) {
        List<SpotifySearchResult> results = new ArrayList<>();
        if (response == null) return new SpotifySearchPage(results, 0, offset, limit);

        // Collect existing media URLs to flag already-imported tracks
        java.util.Set<String> existingUrls = new java.util.HashSet<>(
            trackRepository.findAll().stream()
                .map(t -> t.getMediaUrl())
                .toList()
        );

        int total = 0;
        try {
            Map<?, ?> tracksObj = (Map<?, ?>) response.get("tracks");
            if (tracksObj == null) return new SpotifySearchPage(results, 0, offset, limit);

            total = num((Map<?, ?>) tracksObj, "total");
            List<?> items = (List<?>) tracksObj.get("items");
            if (items == null) return new SpotifySearchPage(results, total, offset, limit);

            results.addAll(parseItems(items, existingUrls));
        } catch (Exception e) {
            log.error("Error parsing Spotify response: {}", e.getMessage());
        }

        return new SpotifySearchPage(results, total, offset, limit);
    }


    /**
     * Unwraps the Spotify API response envelope and delegates item parsing.
     * Used by {@link #trending} to share parsing logic.
     */
    @SuppressWarnings("unchecked")
    private List<SpotifySearchResult> parseItems(Map<?, ?> response,
                                                  java.util.Set<String> existingUrls) {
        List<SpotifySearchResult> out = new ArrayList<>();
        if (response == null) return out;
        try {
            Map<?, ?> tracksObj = (Map<?, ?>) response.get("tracks");
            if (tracksObj == null) return out;
            List<?> items = (List<?>) tracksObj.get("items");
            if (items != null) out.addAll(parseItems(items, existingUrls));
        } catch (Exception e) {
            log.warn("parseItems(response) error: {}", e.getMessage());
        }
        return out;
    }

    /** Parses a raw Spotify items list into {@link SpotifySearchResult} objects. */
    @SuppressWarnings("unchecked")
    private List<SpotifySearchResult> parseItems(List<?> items,
                                                  java.util.Set<String> existingUrls) {
        List<SpotifySearchResult> out = new ArrayList<>();
        for (Object item : items) {
            try {
                Map<?, ?> track = (Map<?, ?>) item;
                if (track == null) continue;

                String id      = str(track, "id");
                String name    = str(track, "name");
                int duration   = num(track, "duration_ms");
                int popularity = num(track, "popularity");

                List<?> artistsList = (List<?>) track.get("artists");
                String artist = "";
                String primaryArtistId = "";
                if (artistsList != null && !artistsList.isEmpty()) {
                    Map<?, ?> primaryArtistMap = (Map<?, ?>) artistsList.get(0);
                    primaryArtistId = str(primaryArtistMap, "id");
                    artist = artistsList.stream()
                        .map(a -> str((Map<?,?>) a, "name"))
                        .filter(s -> !s.isBlank())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                }

                String album = "", imageUrl = "", releaseDate = "";
                Map<?, ?> albumObj = (Map<?, ?>) track.get("album");
                if (albumObj != null) {
                    album       = str(albumObj, "name");
                    releaseDate = str(albumObj, "release_date");
                    List<?> images = (List<?>) albumObj.get("images");
                    if (images != null && !images.isEmpty()) {
                        imageUrl = str((Map<?,?>) images.get(0), "url");
                    }
                }

                String spotifyUrl = "";
                Map<?, ?> external = (Map<?, ?>) track.get("external_urls");
                if (external != null) spotifyUrl = str(external, "spotify");

                String embedUrl = id.isBlank() ? "" : "https://open.spotify.com/embed/track/" + id;

                SpotifySearchResult result = new SpotifySearchResult(
                    id, name, artist, album, imageUrl, spotifyUrl, embedUrl, duration, popularity
                );
                result.setReleaseDate(releaseDate);
                result.setPrimaryArtistId(primaryArtistId);
                result.setAlreadyImported(existingUrls.contains(spotifyUrl));
                out.add(result);
            } catch (Exception e) {
                log.warn("Skipping unparseable track: {}", e.getMessage());
            }
        }
        return out;
    }

    // ── Metadata Refresh ──────────────────────────────────────────────────────

    /** Extracts the Spotify track ID from a Spotify or Spotify embed URL. */
    private String extractSpotifyTrackId(String url) {
        if (url == null || url.isBlank()) return null;
        Matcher m = TRACK_ID_RE.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    /** Extracts the Spotify artist ID from a Spotify artist URL. */
    private String extractSpotifyArtistId(String url) {
        if (url == null || url.isBlank()) return null;
        Matcher m = ARTIST_ID_RE.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Refreshes a single track's title, creator, and artwork from Spotify.
     * Only acts on tracks with platformSource == "Spotify".
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> refreshTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
            .orElseThrow(() -> new RuntimeException("Track not found: " + trackId));

        List<String> updated = new ArrayList<>();

        if ("Spotify".equalsIgnoreCase(track.getPlatformSource())) {
            String spotifyId = extractSpotifyTrackId(track.getMediaUrl());
            if (spotifyId == null) spotifyId = extractSpotifyTrackId(track.getEmbedUrl());
            
            if (spotifyId != null) {
                String token = getAccessToken();
                if (token != null) {
                    try {
                        Map<?, ?> resp = restClient.get()
                            .uri(TRACKS_URL + spotifyId)
                            .header("Authorization", "Bearer " + token)
                            .retrieve().body(Map.class);
                        
                        if (resp != null) {
                            String name = str((Map<?, ?>) resp, "name");
                            if (!name.isBlank() && !name.equals(track.getTitle())) {
                                track.setTitle(name); updated.add("title");
                            }

                            List<?> artists = (List<?>) resp.get("artists");
                            String primaryArtistId = null;
                            if (artists != null && !artists.isEmpty()) {
                                Map<?, ?> primaryArtistMap = (Map<?, ?>) artists.get(0);
                                primaryArtistId = str(primaryArtistMap, "id");
                                String artistName = artists.stream()
                                    .map(a -> str((Map<?, ?>) a, "name"))
                                    .filter(s -> !s.isBlank())
                                    .reduce((a, b) -> a + ", " + b).orElse("");
                                if (!artistName.isBlank() && !artistName.equals(track.getCreator())) {
                                    track.setCreator(artistName); updated.add("creator");
                                }
                            }

                            if (primaryArtistId != null && !primaryArtistId.isBlank()) {
                                String genre = fetchArtistGenre(primaryArtistId);
                                if (genre == null || genre.isBlank()) {
                                    // Fallback to local Artist DB
                                    com.mikstermedia.model.Artist localArtist = artistRepository.findByNameIgnoreCase(track.getCreator()).orElse(null);
                                    if (localArtist != null && localArtist.getPrimaryGenre() != null && !localArtist.getPrimaryGenre().isBlank()) {
                                        genre = localArtist.getPrimaryGenre();
                                    }
                                }
                                if (genre != null && !genre.isBlank() && !genre.equals(track.getGenre())) {
                                    track.setGenre(genre); 
                                    updated.add("genre");
                                }
                            }

                            Map<?, ?> album = (Map<?, ?>) resp.get("album");
                            if (album != null) {
                                List<?> images = (List<?>) album.get("images");
                                if (images != null && !images.isEmpty()) {
                                    String img = str((Map<?, ?>) images.get(0), "url");
                                    if (!img.isBlank() && !img.equals(track.getImageUrl())) {
                                        track.setImageUrl(img); updated.add("imageUrl");
                                    }
                                }
                                String rDate = str((Map<?, ?>) album, "release_date");
                                if (!rDate.isBlank() && !rDate.equals(track.getReleaseDate())) {
                                    track.setReleaseDate(rDate); updated.add("releaseDate");
                                }
                            }

                            int popularity = num((Map<?, ?>) resp, "popularity");
                            if (track.getSpotifyPopularity() == null || popularity != track.getSpotifyPopularity()) {
                                track.setSpotifyPopularity(popularity);
                                updated.add("spotifyPopularity");
                            }
                        }
                    } catch (Exception e) {
                        log.error("Spotify refresh failed for track {}: {}", trackId, e.getMessage());
                    }
                }
            }
        }

        // ── Auto-create artist(s) if missing ────────────────────────────────────
        // After a refresh may have updated creator name, ensure each named artist exists.
        String creatorField = track.getCreator();
        if (creatorField != null && !creatorField.isBlank()) {
            for (String raw : creatorField.split(",")) {
                String artistName = raw.trim();
                if (artistName.isBlank()) continue;
                boolean exists = artistRepository
                    .findByNameContainingIgnoreCase(artistName)
                    .stream()
                    .anyMatch(a -> a.getName().equalsIgnoreCase(artistName));
                if (!exists) {
                    Artist newArtist = new Artist();
                    newArtist.setName(artistName);
                    newArtist.setAiToolsUsed(track.getPlatformSource() != null
                        ? track.getPlatformSource() + " Import" : "Import");
                    if (track.getImageUrl() != null && !track.getImageUrl().isBlank()) {
                        newArtist.setImageUrl(track.getImageUrl());
                    }
                    artistRepository.save(newArtist);
                    updated.add("autoCreatedArtist:" + artistName);
                    log.info("Auto-created artist '{}' during track refresh for track {}", artistName, trackId);
                }
            }
        }

        // ── Backfill videoUrl from mediaUrl if the link is a YouTube URL ─────────
        // Handles tracks imported before videoUrl was captured (legacy data fix).
        String existingVideoUrl = track.getVideoUrl();
        String mediaUrl = track.getMediaUrl();
        if ((existingVideoUrl == null || existingVideoUrl.isBlank())
                && mediaUrl != null
                && (mediaUrl.contains("youtube.com") || mediaUrl.contains("youtu.be"))) {
            track.setVideoUrl(mediaUrl);
            updated.add("backfilledVideoUrl");
            log.info("Backfilled videoUrl from mediaUrl for track {}", trackId);
        }

        try {
            Integer scrobbles = lastFmService.getScrobbles(track.getTitle(), track.getCreator());
            if (track.getLastFmScrobbles() == null || !scrobbles.equals(track.getLastFmScrobbles())) {
                track.setLastFmScrobbles(scrobbles);
                updated.add("lastFmScrobbles");
            }
        } catch (Exception e) {
            log.error("Last.fm refresh failed for track {}: {}", trackId, e.getMessage());
        }

        try {
            // Use videoUrl if set, otherwise fall back to mediaUrl for YouTube-platform tracks
            String ytUrl = track.getVideoUrl();
            if (ytUrl == null || ytUrl.isBlank()) ytUrl = track.getMediaUrl();

            if (ytUrl != null && !ytUrl.isBlank() && (ytUrl.contains("youtube.com") || ytUrl.contains("youtu.be"))) {
                Long views = youTubeService.getViews(ytUrl);
                if (track.getYoutubeViews() == null || !views.equals(track.getYoutubeViews())) {
                    track.setYoutubeViews(views.intValue());
                    updated.add("youtubeViews");
                }
            }
        } catch (Exception e) {
            log.error("YouTube refresh failed for track {}: {}", trackId, e.getMessage());
        }

        // --- New Integrations ---
        try {
            sunoScraperService.refreshMetrics(track);
            updated.add("sunoMetrics");
        } catch (Exception e) {
            log.error("Suno refresh failed for track {}: {}", trackId, e.getMessage());
        }

        try {
            udioScraperService.refreshMetrics(track);
            updated.add("udioMetrics");
        } catch (Exception e) {
            log.error("Udio refresh failed for track {}: {}", trackId, e.getMessage());
        }

        // Chartmetric integration disabled — re-enable when subscription is active

        trackRepository.save(track);
        
        // Ensure the global chart stays up to date after fetching new stats
        weeklyChartService.recalculateRankings();
        
        log.info("Refreshed track {} '{}': {}", trackId, track.getTitle(), updated);
        return Map.of("refreshed", true, "updated", updated, "title", track.getTitle());
    }

    /**
     * Refreshes a single artist's image, follower count, and primary genre from Spotify.
     * Looks up by stored profileUrl (Spotify artist URL) or falls back to name search.
     * On first name-search match, saves the Spotify artist URL to profileUrl.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> refreshArtist(Long artistId) {
        Artist artist = artistRepository.findById(artistId)
            .orElseThrow(() -> new RuntimeException("Artist not found: " + artistId));

        String token = getAccessToken();
        if (token == null) return Map.of("refreshed", false, "reason", "No Spotify token available");

        try {
            Map<?, ?> artistData = null;
            String spotifyArtistId = extractSpotifyArtistId(artist.getProfileUrl());

            if (spotifyArtistId != null) {
                // Direct lookup — fastest path
                artistData = restClient.get()
                    .uri(ARTISTS_URL + spotifyArtistId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve().body(Map.class);
            } else {
                // Search by name
                String enc = java.net.URLEncoder.encode(artist.getName(),
                    java.nio.charset.StandardCharsets.UTF_8);
                Map<?, ?> sr = restClient.get()
                    .uri(SEARCH_URL + "?q=" + enc + "&type=artist&limit=1")
                    .header("Authorization", "Bearer " + token)
                    .retrieve().body(Map.class);
                if (sr != null) {
                    Map<?, ?> ao = (Map<?, ?>) sr.get("artists");
                    if (ao != null) {
                        List<?> items = (List<?>) ao.get("items");
                        if (items != null && !items.isEmpty()) {
                            artistData = (Map<?, ?>) items.get(0);
                            // Save Spotify artist URL for future direct lookups
                            String sid = str(artistData, "id");
                            if (!sid.isBlank()) {
                                artist.setProfileUrl("https://open.spotify.com/artist/" + sid);
                            }
                        }
                    }
                }
            }

            if (artistData == null) {
                return Map.of("refreshed", false, "reason", "Artist not found on Spotify");
            }

            List<String> updated = new ArrayList<>();

            List<?> images = (List<?>) artistData.get("images");
            if (images != null && !images.isEmpty()) {
                String img = str((Map<?, ?>) images.get(0), "url");
                if (!img.isBlank() && !img.equals(artist.getImageUrl())) {
                    artist.setImageUrl(img); updated.add("imageUrl");
                }
            }

            Map<?, ?> followers = (Map<?, ?>) artistData.get("followers");
            if (followers != null) {
                int total = num(followers, "total");
                if (total != artist.getMonthlyListeners()) {
                    artist.setMonthlyListeners(total); updated.add("monthlyListeners");
                }
            }

            List<?> genres = (List<?>) artistData.get("genres");
            if (genres != null && !genres.isEmpty() &&
                    (artist.getPrimaryGenre() == null || artist.getPrimaryGenre().isBlank())) {
                artist.setPrimaryGenre(genres.get(0).toString()); updated.add("primaryGenre");
            }

            artistRepository.save(artist);
            log.info("Refreshed artist {} '{}': {}", artistId, artist.getName(), updated);
            return Map.of("refreshed", true, "updated", updated, "name", artist.getName());
        } catch (Exception e) {
            log.error("Artist refresh failed for {}: {}", artistId, e.getMessage());
            return Map.of("refreshed", false, "reason", e.getMessage());
        }
    }

    /** Async Bulk-refreshes every Spotify track in the library. Updates internal state. */
    @org.springframework.scheduling.annotation.Async
    public void startRefreshAllTracks() {
        if (isTracksRefreshing) return;
        isTracksRefreshing = true;
        tracksRefreshTotal = 0;
        tracksRefreshCompleted = 0;
        tracksRefreshErrors = 0;

        try {
            List<Track> spotifyTracks = trackRepository.findAll().stream()
                .filter(t -> "Spotify".equalsIgnoreCase(t.getPlatformSource()))
                .toList();
                
            tracksRefreshTotal = spotifyTracks.size();
            int refreshed = 0, skipped = 0;
            
            for (Track t : spotifyTracks) {
                Map<String, Object> r = refreshTrack(t.getId());
                if (Boolean.TRUE.equals(r.get("refreshed")))     refreshed++;
                else if (r.get("reason") != null &&
                         r.get("reason").toString().contains("Not a Spotify")) skipped++;
                else tracksRefreshErrors++;
                
                tracksRefreshCompleted++;
                try { Thread.sleep(100); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); break;
                }
            }
            log.info("Async bulk track refresh: {}/{} refreshed, {} errors",
                     refreshed, tracksRefreshTotal, tracksRefreshErrors);
        } finally {
            isTracksRefreshing = false;
        }
    }

    /** Async Bulk-refreshes every artist's Spotify metadata. Updates internal state. */
    @org.springframework.scheduling.annotation.Async
    public void startRefreshAllArtists() {
        if (isArtistsRefreshing) return;
        isArtistsRefreshing = true;
        artistsRefreshTotal = 0;
        artistsRefreshCompleted = 0;
        artistsRefreshErrors = 0;

        try {
            List<Artist> all = artistRepository.findAll();
            artistsRefreshTotal = all.size();
            int refreshed = 0, skipped = 0;
            
            for (Artist a : all) {
                Map<String, Object> r = refreshArtist(a.getId());
                if (Boolean.TRUE.equals(r.get("refreshed")))              refreshed++;
                else if ("Artist not found on Spotify".equals(r.get("reason"))) skipped++;
                else artistsRefreshErrors++;
                
                artistsRefreshCompleted++;
                try { Thread.sleep(100); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); break;
                }
            }
            log.info("Async bulk artist refresh: {}/{} refreshed, {} errors",
                     refreshed, artistsRefreshTotal, artistsRefreshErrors);
        } finally {
            isArtistsRefreshing = false;
        }
    }

    /** Returns current refresh progress state */
    public Map<String, Object> getRefreshStatus() {
        return Map.of(
            "tracks", Map.of(
                "isRefreshing", isTracksRefreshing,
                "total", tracksRefreshTotal,
                "completed", tracksRefreshCompleted,
                "errors", tracksRefreshErrors
            ),
            "artists", Map.of(
                "isRefreshing", isArtistsRefreshing,
                "total", artistsRefreshTotal,
                "completed", artistsRefreshCompleted,
                "errors", artistsRefreshErrors
            )
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String str(Map<?, ?> map, String key) {
        Object val = map == null ? null : map.get(key);
        return val instanceof String s ? s : "";
    }

    private int num(Map<?, ?> m, String k) {
        if (m == null || m.get(k) == null) return 0;
        if (m.get(k) instanceof Number n) return n.intValue();
        return 0;
    }

    /**
     * Fetches the primary genre for a given Spotify artist ID.
     */
    public String fetchArtistGenre(String artistId) {
        if (artistId == null || artistId.isBlank()) return null;
        String token = getAccessToken();
        if (token == null) return null;
        try {
            Map<?, ?> artistData = restClient.get()
                .uri(ARTISTS_URL + artistId)
                .header("Authorization", "Bearer " + token)
                .retrieve().body(Map.class);
            if (artistData != null) {
                List<?> genres = (List<?>) artistData.get("genres");
                if (genres != null && !genres.isEmpty()) {
                    String genre = genres.get(0).toString();
                    return genre.substring(0, 1).toUpperCase() + genre.substring(1);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch artist genre for {}: {}", artistId, e.getMessage());
        }
        return null;
    }
}
