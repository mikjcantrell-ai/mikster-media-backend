package com.mikstermedia.config;

import com.mikstermedia.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner implements ApplicationRunner {

    private final TrackRepository trackRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting database migrations in a background thread to prevent startup hanging...");

        new Thread(() -> {
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
        }).start();
    }
}
