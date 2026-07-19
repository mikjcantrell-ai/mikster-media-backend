package com.mikstermedia.config;

import com.mikstermedia.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner {

    private final TrackRepository trackRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1) // Run BEFORE WeeklyChartService.recalculateRankings() which is not ordered (defaults to lowest precedence)
    public void runMigrations() {
        log.info("Starting database migrations sequentially to prevent MySQL metadata lock deadlock...");

        try {
            int restored = trackRepository.restoreUpvotesFromWeeklyChart();
            if (restored > 0) {
                log.info("Successfully restored {} upvotes from weekly_chart to track table!", restored);
            }
        } catch (Exception e) {
            log.warn("Failed to restore upvotes: {}", e.getMessage());
        }

        try {
            // Fix the 500 error by allowing NULLs on the orphaned upvote_count column
            trackRepository.alterWeeklyChartToAllowNulls();
            log.info("Successfully altered weekly_chart.upvote_count to allow NULLs");
        } catch (Exception e) {
            log.warn("Failed to alter weekly_chart (column might not exist or already nullable): {}", e.getMessage());
        }

        log.info("Database migrations completed.");
    }
}
