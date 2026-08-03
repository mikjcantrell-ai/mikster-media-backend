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
    private final jakarta.persistence.EntityManager em;

    @PostMapping("/run")
    public ResponseEntity<String> runMigrations() {
        log.info("Manual migration triggered via API...");
        StringBuilder result = new StringBuilder("Migration Results:\n");

        try {
            int restored = trackRepository.restoreUpvotesNative();
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

        // Add soundcloud_plays column if not already present
        try {
            if (trackRepository.countSoundcloudPlaysColumn() == 0) {
                trackRepository.addSoundcloudPlaysColumn();
                result.append("Added soundcloud_plays column.\n");
            } else {
                result.append("soundcloud_plays column already present.\n");
            }
            log.info("soundcloud_plays column checked/added on tracks table");
        } catch (Exception e) {
            result.append("Failed to add soundcloud_plays: ").append(e.getMessage()).append("\n");
            log.warn("Failed to add soundcloud_plays column: {}", e.getMessage());
        }

        // Clean up accidentally imported non-AI tracks (e.g. Snow Patrol, Daddy Yankee, Snow Man)
        try {
            int chartsDeleted = trackRepository.deleteNonAiWeeklyCharts();
            int tracksDeleted = trackRepository.deleteNonAiTracks();
            result.append("Removed ").append(tracksDeleted).append(" non-AI tracks (and ").append(chartsDeleted).append(" chart records).\n");
            log.info("Cleaned up {} non-AI tracks from tracks table ({} charts)", tracksDeleted, chartsDeleted);
        } catch (Exception e) {
            result.append("Failed to clean up non-AI tracks: ").append(e.getMessage()).append("\n");
            log.warn("Failed to clean up non-AI tracks: {}", e.getMessage());
        }

        try {
            try {
                em.createNativeQuery("ALTER TABLE email_blast ADD COLUMN type VARCHAR(50) DEFAULT 'News Letter'").executeUpdate();
                result.append("Added 'type' column to email_blast.\n");
            } catch (Exception colEx) {
                result.append("type column check: ").append(colEx.getMessage()).append("\n");
            }
            int updated = em.createNativeQuery("UPDATE email_blast SET type = 'News Letter' WHERE type IS NULL OR type = ''").executeUpdate();
            result.append("Updated ").append(updated).append(" email_blast records to type='News Letter'.\n");
            log.info("Migrated email_blast records: {} updated", updated);
        } catch (Exception e) {
            result.append("Failed to migrate email_blast: ").append(e.getMessage()).append("\n");
            log.warn("Failed to migrate email_blast: {}", e.getMessage());
        }
        try {
            em.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS ai_generators (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    slug VARCHAR(255) NOT NULL UNIQUE,
                    name VARCHAR(255) NOT NULL,
                    logo VARCHAR(255) NOT NULL,
                    tagline TEXT,
                    category VARCHAR(255) NOT NULL,
                    website_url VARCHAR(255) NOT NULL,
                    rating DOUBLE,
                    verdict TEXT,
                    free_tier TEXT,
                    pro_tier TEXT,
                    premier_tier TEXT,
                    max_track_length VARCHAR(255),
                    has_stems BOOLEAN NOT NULL DEFAULT FALSE,
                    has_inpainting BOOLEAN NOT NULL DEFAULT FALSE,
                    has_custom_lyrics BOOLEAN NOT NULL DEFAULT FALSE,
                    has_midi_export BOOLEAN NOT NULL DEFAULT FALSE,
                    commercial_rights_on_paid BOOLEAN NOT NULL DEFAULT FALSE,
                    audio_quality VARCHAR(255),
                    best_for_tags TEXT,
                    display_order INT,
                    url_reachable BOOLEAN NOT NULL DEFAULT TRUE,
                    last_verified_at DATETIME(6)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """).executeUpdate();
            result.append("Checked/created ai_generators table.\n");
        } catch (Exception e) {
            result.append("ai_generators table check: ").append(e.getMessage()).append("\n");
        }

        return ResponseEntity.ok(result.toString());
    }

    @PostMapping("/kill-locks")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> killLocks() {
        log.info("Attempting to kill zombie MySQL connections...");
        StringBuilder result = new StringBuilder("Killed Connections:\n");
        try {
            java.util.List<Object[]> processList = em.createNativeQuery("SHOW FULL PROCESSLIST").getResultList();
            for (Object[] row : processList) {
                Long id = ((Number) row[0]).longValue();
                String command = (String) row[4];
                Integer time = ((Number) row[5]).intValue();
                
                // Kill ANY connection (Query or Sleep) older than 30 seconds
                if (time > 30) {
                    try {
                        em.createNativeQuery("KILL " + id).executeUpdate();
                        result.append("Killed connection ID ").append(id).append(" (sleeping for ").append(time).append("s)\n");
                    } catch (Exception killEx) {
                        result.append("Failed to kill connection ID ").append(id).append(": ").append(killEx.getMessage()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to read process list: " + e.getMessage());
        }
        return ResponseEntity.ok(result.toString());
    }
}
