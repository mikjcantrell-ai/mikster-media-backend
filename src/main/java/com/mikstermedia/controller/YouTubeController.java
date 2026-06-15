package com.mikstermedia.controller;

import com.mikstermedia.dto.TrackDTO;
import com.mikstermedia.dto.YouTubeSearchPage;
import com.mikstermedia.dto.YouTubeSearchResult;
import com.mikstermedia.model.Artist;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.service.TrackService;
import com.mikstermedia.service.YouTubeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
@Slf4j
public class YouTubeController {

    private final YouTubeService youtubeService;
    private final TrackService trackService;
    private final ArtistRepository artistRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "configured", youtubeService.isConfigured()
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<YouTubeSearchPage> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String pageToken) {
        return ResponseEntity.ok()
            .header("Cache-Control", "no-store, no-cache, must-revalidate")
            .body(youtubeService.search(q, limit, pageToken));
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importTrack(@RequestBody YouTubeSearchResult candidate) {
        // 1. Auto-create channel/artist
        String channelName = candidate.getChannelTitle();
        if (channelName != null && !channelName.isBlank()) {
            boolean exists = artistRepository
                .findByNameContainingIgnoreCase(channelName)
                .stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(channelName));

            if (!exists) {
                Artist artist = new Artist();
                artist.setName(channelName);
                artist.setAiToolsUsed("YouTube Import");
                artistRepository.save(artist);
                log.info("Auto-created artist/channel '{}' from YouTube import", channelName);
            }
        }

        // 2. Create Track
        TrackDTO dto = new TrackDTO();
        dto.setTitle(candidate.getTitle());
        dto.setCreator(candidate.getChannelTitle());
        dto.setMediaUrl(candidate.getYoutubeUrl());
        dto.setPlatformSource("YouTube");
        dto.setEmbedUrl(candidate.getEmbedUrl());
        dto.setImageUrl(candidate.getThumbnailUrl());
        dto.setAiToolsUsed("");
        dto.setGenre(""); // YouTube API doesn't provide track genres readily
        dto.setFeaturedStatus(false);
        dto.setYoutubeViews(candidate.getViews());
        
        // Convert YouTube ISO date to YYYY-MM-DD
        String published = candidate.getPublishedAt();
        if (published != null && published.length() >= 10) {
            dto.setReleaseDate(published.substring(0, 10));
        } else {
            dto.setReleaseDate("");
        }

        trackService.createTrack(dto);
        return ResponseEntity.noContent().build();
    }
}
