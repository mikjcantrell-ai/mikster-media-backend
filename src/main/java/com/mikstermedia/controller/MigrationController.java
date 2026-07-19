package com.mikstermedia.controller;

import com.mikstermedia.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/migrations")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final TrackRepository trackRepository;

    @PostMapping("/run")
    public ResponseEntity<String> runMigrations() {
        log.info("Manual migration triggered via API...");
        StringBuilder result = new StringBuilder("Migration Results:\n");

        try {
            int restored = trackRepository.restoreUpvotesFromWeeklyChart();
            result.append("Restored ").append(restored).append(" upvotes.\n");
            log.info("Successfully restored {} upvotes from weekly_chart to track table!", restored);
        } catch (Exception e) {
            result.append("Failed to restore upvotes: ").append(e.getMessage()).append("\n");
            log.warn("Failed to restore upvotes: {}", e.getMessage());
        }

        try {
            trackRepository.alterWeeklyChartToAllowNulls();
            result.append("Altered weekly_chart upvote_count to allow NULLs.\n");
            log.info("Successfully altered weekly_chart.upvote_count to allow NULLs");
        } catch (Exception e) {
            result.append("Failed to alter weekly_chart: ").append(e.getMessage()).append("\n");
            log.warn("Failed to alter weekly_chart: {}", e.getMessage());
        }

        return ResponseEntity.ok(result.toString());
    }
}
