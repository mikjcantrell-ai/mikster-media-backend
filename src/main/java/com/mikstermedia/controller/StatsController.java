package com.mikstermedia.controller;

import com.mikstermedia.repository.ArtistRepository;
import com.mikstermedia.repository.MemberRepository;
import com.mikstermedia.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public read-only endpoint that exposes live library counts for the homepage hero stats.
 *
 * <p>GET /api/stats — returns totalTracks, platformCount, totalArtists, memberCount.
 * No authentication required (SecurityConfig allows all GETs).
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final TrackRepository  trackRepository;
    private final ArtistRepository artistRepository;
    private final MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalTracks",   trackRepository.count(),
            "platformCount", trackRepository.countDistinctPlatforms(),
            "totalArtists",  artistRepository.count(),
            "memberCount",   memberRepository.count()
        ));
    }
}
