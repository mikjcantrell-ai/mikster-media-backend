package com.mikstermedia.controller;

import com.mikstermedia.model.WeeklyChart;
import com.mikstermedia.model.Track;
import com.mikstermedia.service.WeeklyChartService;
import com.mikstermedia.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Top-10 Weekly AI Chart leaderboard.
 *
 * <p>Base path: {@code /api/charts}
 *
 * <p>Integration notes for Angular:
 * <ul>
 *   <li>ChartsComponent calls {@code GET /api/charts/top10} on init to render
 *       the leaderboard table.</li>
 *   <li>The inline play button triggers {@code POST /api/charts/{id}/play} so
 *       the play count is persisted before the embed widget loads.</li>
 *   <li>The upvote button sends {@code POST /api/charts/{id}/upvote}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class WeeklyChartController {

    private final WeeklyChartService weeklyChartService;
    private final TrackService trackService;
    private final com.mikstermedia.service.SpotifyService spotifyService;

    /**
     * GET /api/charts/top10
     * Returns up to 10 chart entries sorted by current_rank ascending.
     * Angular ChartsComponent binds directly to this response.
     */
    @GetMapping("/top10")
    public ResponseEntity<List<WeeklyChart>> getTopChart() {
        List<WeeklyChart> chart = weeklyChartService.getTopChart();
        // Cap response at 10 entries for the leaderboard display
        List<WeeklyChart> top10 = chart.stream().limit(10).toList();
        return ResponseEntity.ok(top10);
    }

    /** GET /api/charts — returns all chart entries (admin view) */
    @GetMapping
    public ResponseEntity<List<WeeklyChart>> getAllChartEntries() {
        return ResponseEntity.ok(weeklyChartService.getTopChart());
    }

    /**
     * POST /api/charts/add/{trackId}
     * Adds an existing track to the weekly chart as a new entry.
     * Called by admin panel when promoting a track to the chart.
     */
    @PostMapping("/add/{trackId}")
    public ResponseEntity<WeeklyChart> addTrackToChart(@PathVariable Long trackId) {
        Track track = trackService.getTrackById(trackId);
        return ResponseEntity.ok(weeklyChartService.addToChart(track));
    }

    /**
     * POST /api/charts/{id}/upvote
     * Records one upvote for the chart entry and triggers score recalculation.
     * Angular upvote button calls this; the response reflects updated rank.
     */
    @PostMapping("/{id}/upvote")
    public ResponseEntity<WeeklyChart> upvote(@PathVariable Long id) {
        return ResponseEntity.ok(weeklyChartService.upvote(id));
    }

    /**
     * POST /api/charts/{id}/play
     * Increments weekly_plays for the entry and triggers recalculation.
     * Called when the Angular inline player starts playback.
     */
    @PostMapping("/{id}/play")
    public ResponseEntity<WeeklyChart> recordPlay(@PathVariable Long id) {
        return ResponseEntity.ok(weeklyChartService.recordPlay(id));
    }

    /**
     * Admin: manually recalculate the rankings based on current stats.
     */
    @PostMapping("/recalculate")
    public ResponseEntity<Map<String, String>> recalculate() {
        // Fetch external stats (YouTube views, Spotify, Last.fm) for the current top 10
        List<WeeklyChart> currentTop10 = weeklyChartService.getTopChart().stream().limit(10).toList();
        for (WeeklyChart entry : currentTop10) {
            try {
                spotifyService.refreshTrack(entry.getTrack().getId());
            } catch (Exception e) {
                // Ignore failure for a single track and continue
            }
        }
        
        // Final recalculation in case any scores changed
        weeklyChartService.recalculateRankings();
        return ResponseEntity.ok(Map.of("message", "Rankings recalculated successfully"));
    }

    /**
     * Admin: manually snapshot the week's stats and reset upvotes.
     */
    @PostMapping("/snapshot")
    public ResponseEntity<Map<String, String>> snapshot() {
        weeklyChartService.snapshotWeek();
        return ResponseEntity.ok(Map.of("message", "Weekly snapshot taken successfully"));
    }
}
