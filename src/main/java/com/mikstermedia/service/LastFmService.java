package com.mikstermedia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@Slf4j
public class LastFmService {

    @Value("${lastfm.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String LASTFM_API_URL = "http://ws.audioscrobbler.com/2.0/";

    public Integer getScrobbles(String trackName, String artistName) {
        if (apiKey == null || apiKey.isEmpty() || "YOUR_KEY_HERE".equals(apiKey)) {
            log.warn("Last.fm API key is not configured.");
            return 0;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(LASTFM_API_URL)
                    .queryParam("method", "track.getInfo")
                    .queryParam("api_key", apiKey)
                    .queryParam("artist", artistName)
                    .queryParam("track", trackName)
                    .queryParam("format", "json")
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("track")) {
                Map<String, Object> trackObj = (Map<String, Object>) response.get("track");
                if (trackObj.containsKey("playcount")) {
                    String playcountStr = trackObj.get("playcount").toString();
                    return Integer.parseInt(playcountStr);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch Last.fm scrobbles for {} - {}: {}", artistName, trackName, e.getMessage());
        }
        return 0;
    }
}
