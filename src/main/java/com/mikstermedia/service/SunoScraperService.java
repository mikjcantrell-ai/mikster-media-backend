package com.mikstermedia.service;

import com.mikstermedia.model.Track;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to scrape/fetch metrics directly from Suno.com URLs.
 * Uses a regex scraper to find embedded play and upvote counts inside the Next.js HTML payload.
 */
@Service
@Slf4j
public class SunoScraperService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Pattern PLAY_PATTERN = Pattern.compile("\"play_count\"\\s*:\\s*(\\d+)");
    private static final Pattern UPVOTE_PATTERN = Pattern.compile("\"upvote_count\"\\s*:\\s*(\\d+)");

    public void refreshMetrics(Track track) {
        String url = track.getAiSourceUrl();
        if (url == null || !url.contains("suno.com")) {
            return;
        }
        
        log.info("Live scraping Suno metrics for track: {}", track.getTitle());
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String html = response.getBody();
            
            if (html != null) {
                Matcher playMatcher = PLAY_PATTERN.matcher(html);
                if (playMatcher.find()) {
                    int plays = Integer.parseInt(playMatcher.group(1));
                    track.setSunoPlays(plays);
                    log.info("Found Suno Plays: {}", plays);
                }

                Matcher upvoteMatcher = UPVOTE_PATTERN.matcher(html);
                if (upvoteMatcher.find()) {
                    int likes = Integer.parseInt(upvoteMatcher.group(1));
                    track.setSunoLikes(likes);
                    log.info("Found Suno Likes: {}", likes);
                }
            }
        } catch (Exception e) {
            log.error("Failed to scrape Suno metrics for url {}. Error: {}", url, e.getMessage());
            // Fail gracefully, keep existing metrics
        }
    }
}
