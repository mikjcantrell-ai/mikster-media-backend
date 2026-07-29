package com.mikstermedia.service;

import com.mikstermedia.model.PlatformSetting;
import com.mikstermedia.repository.PlatformSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Scheduled background job service that runs twice daily (midnight and noon UTC).
 *
 * <p>Job sequence:
 * <ol>
 *   <li>Kick off async bulk track refresh via {@link SpotifyService#startRefreshAllTracks()}</li>
 *   <li>Poll until the refresh completes (checks every 10 s, times out after 30 min)</li>
 *   <li>Recalculate the Top-10 chart via {@link WeeklyChartService#recalculateRankings()}</li>
 *   <li>Persist completion timestamps in {@code platform_settings}:
 *       {@code tracks_last_updated} and {@code chart_last_updated}</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobService {

    private static final String KEY_TRACKS = "tracks_last_updated";
    private static final String KEY_CHART  = "chart_last_updated";

    /** Max time (ms) to wait for the track refresh to complete before giving up. */
    private static final long REFRESH_TIMEOUT_MS = 30L * 60 * 1000; // 30 minutes
    private static final long POLL_INTERVAL_MS   = 10_000L;           // 10 seconds

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a 'UTC'")
                             .withZone(ZoneId.of("UTC"));

    private final SpotifyService            spotifyService;
    private final WeeklyChartService        weeklyChartService;
    private final AiDiscoveryService        aiDiscoveryService;
    private final SoundCloudService         soundCloudService;
    private final PlatformSettingRepository settingRepository;

    // ── Scheduled entry point ──────────────────────────────────────────────────

    /**
     * Runs at midnight (0:00) and noon (12:00) every day UTC.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 0,12 * * *", zone = "UTC")
    public void runDailyRefreshAndChart() {
        log.info("=== Scheduled job START: track refresh + chart recalculation + AI discovery ===");
        try {
            refreshTracksAndWait();
            recalculateChart();
            kickOffAiDiscovery();
            refreshSoundCloudPlays();
        } catch (Exception e) {
            log.error("Scheduled job encountered an error", e);
        }
        log.info("=== Scheduled job COMPLETE ===");
    }

    // ── Phase 1: Track refresh ─────────────────────────────────────────────────

    private void refreshTracksAndWait() throws InterruptedException {
        Map<String, Object> status = spotifyService.getRefreshStatus();
        boolean alreadyRunning = Boolean.TRUE.equals(status.get("tracks_refreshing"));

        if (!alreadyRunning) {
            log.info("Scheduled: kicking off async track refresh...");
            spotifyService.startRefreshAllTracks();
            // Give the async thread a moment to start
            Thread.sleep(2_000);
        } else {
            log.info("Scheduled: track refresh already in progress — waiting for it to finish...");
        }

        long started = System.currentTimeMillis();
        while (true) {
            Map<String, Object> s = spotifyService.getRefreshStatus();
            boolean stillRunning = Boolean.TRUE.equals(s.get("tracks_refreshing"));
            if (!stillRunning) {
                log.info("Scheduled: track refresh completed.");
                break;
            }
            long elapsed = System.currentTimeMillis() - started;
            if (elapsed > REFRESH_TIMEOUT_MS) {
                log.warn("Scheduled: track refresh timed out after 30 min — proceeding to chart recalculation anyway.");
                break;
            }
            int completed = s.get("tracks_completed") instanceof Number n ? n.intValue() : 0;
            int total     = s.get("tracks_total")     instanceof Number n ? n.intValue() : 0;
            log.info("Scheduled: track refresh in progress ({}/{})...", completed, total);
            Thread.sleep(POLL_INTERVAL_MS);
        }

        // Persist the tracks timestamp
        String ts = FORMATTER.format(Instant.now());
        settingRepository.save(new PlatformSetting(KEY_TRACKS, ts));
        log.info("Scheduled: tracks_last_updated = {}", ts);
    }

    // ── Phase 2: Chart recalculation ──────────────────────────────────────────

    private void recalculateChart() {
        log.info("Scheduled: recalculating Top-10 chart...");
        weeklyChartService.recalculateRankings();

        String ts = FORMATTER.format(Instant.now());
        settingRepository.save(new PlatformSetting(KEY_CHART, ts));
        log.info("Scheduled: chart_last_updated = {}", ts);
    }

    // ── Phase 3: AI discovery + auto-import ───────────────────────────────────

    /**
     * Fires off the AI discovery scan in the background.
     * Results are auto-imported to the library; no admin action required.
     * Runs asynchronously so it doesn't block the scheduled job thread.
     */
    private void kickOffAiDiscovery() {
        log.info("Scheduled: kicking off AI discovery auto-import...");
        aiDiscoveryService.startDiscoveryFetchWithAutoImport();
        // Intentionally non-blocking — the import runs in its own thread.
        // Progress can be monitored via GET /api/ai-discovery/status.
    }

    // ── Phase 4: SoundCloud play count refresh ─────────────────────────

    /**
     * Synchronously refreshes SoundCloud playback counts for all SC-hosted tracks.
     * Runs after chart recalculation so the new counts feed the next chart cycle.
     */
    private void refreshSoundCloudPlays() {
        log.info("Scheduled: refreshing SoundCloud play counts...");
        soundCloudService.refreshAllSoundCloudTracks();
        log.info("Scheduled: SoundCloud play count refresh complete.");
    }
}
