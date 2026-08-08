package com.mikstermedia.controller;

import com.mikstermedia.model.Artist;
import com.mikstermedia.model.Track;
import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.TrackRepository;
import com.mikstermedia.service.LlmContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@Slf4j
public class AdminContentController {

    private final LlmContentService llmContentService;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;

    @PostMapping("/generate/track/{id}")
    public ResponseEntity<Map<String, Object>> generateTrackReview(@PathVariable Long id) {
        Track track = trackRepository.findById(id).orElse(null);
        if (track == null) {
            return ResponseEntity.notFound().build();
        }

        String review = llmContentService.generateTrackReview(track);
        if (review != null && !review.isBlank()) {
            track.setDescription(review);
            trackRepository.save(track);
            return ResponseEntity.ok(Map.of("success", true, "description", review));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to generate"));
    }

    @PostMapping("/generate/artist/{id}")
    public ResponseEntity<Map<String, Object>> generateArtistBio(@PathVariable Long id) {
        Artist artist = artistRepository.findById(id).orElse(null);
        if (artist == null) {
            return ResponseEntity.notFound().build();
        }

        String bio = llmContentService.generateArtistBio(artist);
        if (bio != null && !bio.isBlank()) {
            artist.setBio(bio);
            artistRepository.save(artist);
            return ResponseEntity.ok(Map.of("success", true, "bio", bio));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to generate"));
    }
}
