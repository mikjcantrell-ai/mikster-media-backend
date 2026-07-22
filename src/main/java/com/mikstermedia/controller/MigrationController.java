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
    @org.springframework.web.bind.annotation.GetMapping("/logs")
    public ResponseEntity<String> getLogs() {
        try {
            java.nio.file.Path logFile = java.nio.file.Paths.get("application.log");
            if (!java.nio.file.Files.exists(logFile)) {
                return ResponseEntity.ok("Log file not found.");
            }
            String logs = java.nio.file.Files.readString(logFile);
            // Return only the last 50000 characters if it's too large
            if (logs.length() > 50000) {
                logs = logs.substring(logs.length() - 50000);
            }
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error reading logs: " + e.getMessage());
        }
    }
}
