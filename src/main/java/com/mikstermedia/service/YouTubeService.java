package com.mikstermedia.service;

import com.mikstermedia.dto.YouTubeSearchPage;
import com.mikstermedia.dto.YouTubeSearchResult;
import com.mikstermedia.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeService {

    private final TrackRepository trackRepository;

    @Value("${youtube.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String YOUTUBE_VIDEOS_API_URL = "https://www.googleapis.com/youtube/v3/videos";
    private static final String YOUTUBE_SEARCH_API_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final Pattern YT_PATTERN = Pattern.compile("(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\u200C\\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*");

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !"YOUR_KEY_HERE".equals(apiKey);
    }

    public Long getViews(String url) {
        if (!isConfigured() || url == null || url.isEmpty()) return 0L;

        String videoId = extractVideoId(url);
        if (videoId == null) return 0L;

        try {
            String apiUrl = UriComponentsBuilder.fromHttpUrl(YOUTUBE_VIDEOS_API_URL)
                    .queryParam("part", "statistics")
                    .queryParam("id", videoId)
                    .queryParam("key", apiKey)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
            if (response != null && response.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                if (!items.isEmpty()) {
                    Map<String, Object> statistics = (Map<String, Object>) items.get(0).get("statistics");
                    if (statistics != null && statistics.containsKey("viewCount")) {
                        return Long.parseLong(statistics.get("viewCount").toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch YouTube views for video ID {}: {}", videoId, e.getMessage());
        }
        return 0L;
    }

    public YouTubeSearchPage search(String query, int limit, String pageToken) {
        if (!isConfigured()) {
            log.warn("YouTube API key is not configured.");
            return new YouTubeSearchPage(List.of(), 0, null, false);
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(YOUTUBE_SEARCH_API_URL)
                    .queryParam("part", "snippet")
                    .queryParam("type", "video")
                    .queryParam("q", query)
                    .queryParam("maxResults", limit)
                    .queryParam("key", apiKey);

            if (pageToken != null && !pageToken.isEmpty()) {
                builder.queryParam("pageToken", pageToken);
            }

            Map<String, Object> response = restTemplate.getForObject(builder.toUriString(), Map.class);
            if (response == null || !response.containsKey("items")) {
                return new YouTubeSearchPage(List.of(), 0, null, false);
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            List<YouTubeSearchResult> results = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Map<String, Object> idObj = (Map<String, Object>) item.get("id");
                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");

                if (idObj != null && snippet != null) {
                    String videoId = (String) idObj.get("videoId");
                    if (videoId == null) continue;

                    String title = (String) snippet.get("title");
                    String channelTitle = (String) snippet.get("channelTitle");
                    String publishedAt = (String) snippet.get("publishedAt");
                    
                    String thumbnailUrl = "";
                    Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                    if (thumbnails != null) {
                        Map<String, Object> high = (Map<String, Object>) thumbnails.get("high");
                        if (high != null && high.get("url") != null) {
                            thumbnailUrl = (String) high.get("url");
                        } else {
                            Map<String, Object> def = (Map<String, Object>) thumbnails.get("default");
                            if (def != null && def.get("url") != null) {
                                thumbnailUrl = (String) def.get("url");
                            }
                        }
                    }

                    // Clean up HTML entities in titles
                    if (title != null) {
                        title = title.replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&");
                    }
                    final String finalTitle = title;

                    String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
                    String embedUrl = "https://www.youtube.com/embed/" + videoId;

                    // YouTube search API doesn't return views in snippet. We could do a second batch request for statistics, 
                    // but for discovery, 0 or null is fine until imported.
                    long views = 0;

                    YouTubeSearchResult result = new YouTubeSearchResult(
                            videoId, finalTitle, channelTitle, thumbnailUrl, youtubeUrl, embedUrl, views, publishedAt
                    );

                    // Check if already in library
                    boolean exists = trackRepository.findAll().stream()
                            .anyMatch(t -> (t.getMediaUrl() != null && t.getMediaUrl().contains(videoId)) ||
                                           (t.getPlatformSource() != null && t.getPlatformSource().equalsIgnoreCase("YouTube") && 
                                            t.getTitle().equalsIgnoreCase(finalTitle)));
                    result.setAlreadyImported(exists);

                    results.add(result);
                }
            }

            int totalResults = 0;
            Map<String, Object> pageInfo = (Map<String, Object>) response.get("pageInfo");
            if (pageInfo != null && pageInfo.get("totalResults") != null) {
                totalResults = ((Number) pageInfo.get("totalResults")).intValue();
            }

            String nextPageTokenResponse = (String) response.get("nextPageToken");
            boolean hasMore = nextPageTokenResponse != null && !nextPageTokenResponse.isEmpty();

            return new YouTubeSearchPage(results, totalResults, nextPageTokenResponse, hasMore);

        } catch (Exception e) {
            log.error("YouTube search failed: {}", e.getMessage());
            return new YouTubeSearchPage(List.of(), 0, null, false);
        }
    }

    public String extractVideoId(String url) {
        Matcher matcher = YT_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
