package com.mikstermedia.service;

import com.mikstermedia.model.Track;
import com.mikstermedia.model.WeeklyChart;
import com.mikstermedia.repository.TrackRepository;
import com.mikstermedia.repository.WeeklyChartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Business-logic layer for the Top-10 Weekly AI Chart.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WeeklyChartService {

    private final WeeklyChartRepository weeklyChartRepository;
    private final TrackRepository trackRepository;

    private static final double UPVOTE_WEIGHT = 10.0;
    private static final double PLAY_WEIGHT   = 1.0;

    @Transactional(readOnly = true)
    public List<WeeklyChart> getTopChart() {
        return weeklyChartRepository.findAllByOrderByCurrentRankAsc();
    }

    public WeeklyChart addToChart(Track track) {
        return weeklyChartRepository.findByTrackId(track.getId())
                .orElseGet(() -> {
                    WeeklyChart entry = new WeeklyChart();
                    entry.setTrack(track);
                    entry.setCurrentRank(99);
                    entry.setPreviousRank(null);
                    entry.setUpvoteCount(0);
                    entry.setWeeklyPlays(0);
                    entry.setRankChange("NEW");
                    return weeklyChartRepository.save(entry);
                });
    }

    public WeeklyChart upvote(Long chartId) {
        WeeklyChart entry = weeklyChartRepository.findById(chartId)
                .orElseThrow(() -> new RuntimeException("Chart entry not found: " + chartId));
        entry.setUpvoteCount(entry.getUpvoteCount() + 1);
        weeklyChartRepository.save(entry);
        recalculateRankings();
        return weeklyChartRepository.findById(chartId).orElseThrow();
    }

    public WeeklyChart recordPlay(Long chartId) {
        WeeklyChart entry = weeklyChartRepository.findById(chartId)
                .orElseThrow(() -> new RuntimeException("Chart entry not found: " + chartId));
        entry.setWeeklyPlays(entry.getWeeklyPlays() + 1);
        weeklyChartRepository.save(entry);
        recalculateRankings();
        return weeklyChartRepository.findById(chartId).orElseThrow();
    }

    /**
     * Recalculates rankings globally.
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void recalculateRankings() {
        List<Track> allTracks = trackRepository.findAll();

        // 1. Compute global score for all tracks
        List<TrackScore> scoredTracks = new ArrayList<>();
        List<TrackScore> zeroScoreTracks = new ArrayList<>();

        for (Track t : allTracks) {
            double globalScore = computeGlobalScore(t);
            TrackScore ts = new TrackScore(t, globalScore);
            if (globalScore > 0) {
                scoredTracks.add(ts);
            } else {
                zeroScoreTracks.add(ts);
            }
        }

        // 2. Sort tracks with score > 0 by score descending
        scoredTracks.sort((a, b) -> Double.compare(b.score, a.score));

        // 3. Sort tracks with score == 0 by id descending (newest)
        zeroScoreTracks.sort((a, b) -> Long.compare(b.track.getId(), a.track.getId()));

        // 4. Take top 10
        List<Track> top10Tracks = new ArrayList<>();
        for (TrackScore ts : scoredTracks) {
            if (top10Tracks.size() < 10) top10Tracks.add(ts.track);
        }
        for (TrackScore ts : zeroScoreTracks) {
            if (top10Tracks.size() < 10) top10Tracks.add(ts.track);
        }

        // 5. Fetch existing WeeklyChart entries mapped by track ID
        List<WeeklyChart> existingEntries = weeklyChartRepository.findAll();
        Map<Long, WeeklyChart> existingMap = existingEntries.stream()
                .collect(Collectors.toMap(e -> e.getTrack().getId(), e -> e));

        // 6. Build new Top 10 chart
        List<WeeklyChart> newChart = new ArrayList<>();
        for (int i = 0; i < top10Tracks.size(); i++) {
            Track t = top10Tracks.get(i);
            WeeklyChart entry = existingMap.get(t.getId());
            
            if (entry == null) {
                entry = new WeeklyChart();
                entry.setTrack(t);
                entry.setUpvoteCount(0);
                entry.setWeeklyPlays(0);
                entry.setPreviousRank(null);
            } else {
                entry.setPreviousRank(entry.getCurrentRank());
                // Remove from map so we know which ones to delete
                existingMap.remove(t.getId());
            }
            newChart.add(entry);
        }

        // 7. Sort the newChart by TOTAL score (global + local upvotes) to determine 1-10 order
        newChart.sort((a, b) -> Double.compare(computeTotalScore(b), computeTotalScore(a)));

        // 8. Assign currentRank and rankChange
        for (int i = 0; i < newChart.size(); i++) {
            WeeklyChart entry = newChart.get(i);
            int newRank = i + 1;

            if (entry.getPreviousRank() == null) {
                entry.setRankChange("NEW");
            } else if (newRank < entry.getPreviousRank()) {
                entry.setRankChange("UP");
            } else if (newRank > entry.getPreviousRank()) {
                entry.setRankChange("DOWN");
            } else {
                entry.setRankChange("STEADY");
            }

            entry.setCurrentRank(newRank);
        }

        // 9. Save new chart and delete stale entries
        log.info("Saving newChart of size: " + newChart.size());
        weeklyChartRepository.saveAll(newChart);
        log.info("Deleting stale entries: " + existingMap.values().size());
        weeklyChartRepository.deleteAll(existingMap.values());
        
        log.info("Global Top 10 chart recalculated.");
    }

    @Transactional
    public void snapshotWeek() {
        List<Track> allTracks = trackRepository.findAll();
        for (Track track : allTracks) {
            track.setLastWeekSpotifyPopularity(track.getSpotifyPopularity() != null ? track.getSpotifyPopularity() : 0);
            track.setLastWeekLastFmScrobbles(track.getLastFmScrobbles() != null ? track.getLastFmScrobbles() : 0);
            track.setLastWeekYoutubeViews(track.getYoutubeViews() != null ? track.getYoutubeViews() : 0L);
            track.setLastWeekTiktokViews(track.getTiktokViews() != null ? track.getTiktokViews() : 0);
            track.setLastWeekSunoPlays(track.getSunoPlays() != null ? track.getSunoPlays() : 0);
            track.setLastWeekSunoLikes(track.getSunoLikes() != null ? track.getSunoLikes() : 0);
            track.setLastWeekUdioPlays(track.getUdioPlays() != null ? track.getUdioPlays() : 0);
            track.setLastWeekUdioLikes(track.getUdioLikes() != null ? track.getUdioLikes() : 0);
            track.setLastWeekChartmetricScore(track.getChartmetricScore() != null ? track.getChartmetricScore() : 0);
        }
        trackRepository.saveAll(allTracks);

        List<WeeklyChart> charts = weeklyChartRepository.findAll();
        for (WeeklyChart chart : charts) {
            chart.setUpvoteCount(0);
        }
        weeklyChartRepository.saveAll(charts);

        log.info("Weekly snapshot taken. Stats and upvotes reset.");
        recalculateRankings();
    }

    private double computeGlobalScore(Track track) {
        // Calculate Weekly Growth (Current - Last Week)
        int weeklySpotifyPop = Math.max(0, (track.getSpotifyPopularity() != null ? track.getSpotifyPopularity() : 0) - (track.getLastWeekSpotifyPopularity() != null ? track.getLastWeekSpotifyPopularity() : 0));
        int weeklyLastFmScrobbles = Math.max(0, (track.getLastFmScrobbles() != null ? track.getLastFmScrobbles() : 0) - (track.getLastWeekLastFmScrobbles() != null ? track.getLastWeekLastFmScrobbles() : 0));
        long weeklyYoutubeViews = Math.max(0L, (track.getYoutubeViews() != null ? track.getYoutubeViews() : 0L) - (track.getLastWeekYoutubeViews() != null ? track.getLastWeekYoutubeViews() : 0L));
        
        int weeklySunoPlays = Math.max(0, (track.getSunoPlays() != null ? track.getSunoPlays() : 0) - (track.getLastWeekSunoPlays() != null ? track.getLastWeekSunoPlays() : 0));
        int weeklySunoLikes = Math.max(0, (track.getSunoLikes() != null ? track.getSunoLikes() : 0) - (track.getLastWeekSunoLikes() != null ? track.getLastWeekSunoLikes() : 0));
        int weeklyUdioPlays = Math.max(0, (track.getUdioPlays() != null ? track.getUdioPlays() : 0) - (track.getLastWeekUdioPlays() != null ? track.getLastWeekUdioPlays() : 0));
        int weeklyUdioLikes = Math.max(0, (track.getUdioLikes() != null ? track.getUdioLikes() : 0) - (track.getLastWeekUdioLikes() != null ? track.getLastWeekUdioLikes() : 0));
        int weeklyTiktokViews = Math.max(0, (track.getTiktokViews() != null ? track.getTiktokViews() : 0) - (track.getLastWeekTiktokViews() != null ? track.getLastWeekTiktokViews() : 0));
        double weeklyScore = (weeklySpotifyPop * 10.0) 
             + (weeklyLastFmScrobbles / 1000.0) 
             + (weeklyYoutubeViews / 250.0)
             + (weeklySunoPlays / 500.0)
             + (weeklySunoLikes / 50.0)
             + (weeklyUdioPlays / 500.0)
             + (weeklyUdioLikes / 50.0)
             + (weeklyTiktokViews / 50000.0);

        // Calculate Lifetime Totals (used primarily as a fallback / tie-breaker)
        int totalSpotifyPop = track.getSpotifyPopularity() != null ? track.getSpotifyPopularity() : 0;
        int totalLastFmScrobbles = track.getLastFmScrobbles() != null ? track.getLastFmScrobbles() : 0;
        long totalYoutubeViews = track.getYoutubeViews() != null ? track.getYoutubeViews() : 0L;
        int totalSunoPlays = track.getSunoPlays() != null ? track.getSunoPlays() : 0;
        int totalSunoLikes = track.getSunoLikes() != null ? track.getSunoLikes() : 0;
        int totalUdioPlays = track.getUdioPlays() != null ? track.getUdioPlays() : 0;
        int totalUdioLikes = track.getUdioLikes() != null ? track.getUdioLikes() : 0;
        int totalTiktokViews = track.getTiktokViews() != null ? track.getTiktokViews() : 0;
        
        double lifetimeScore = (totalSpotifyPop * 10.0) 
             + (totalLastFmScrobbles / 100.0) 
             + (totalYoutubeViews / 500.0)
             + (totalSunoPlays / 500.0)
             + (totalSunoLikes / 50.0)
             + (totalUdioPlays / 500.0)
             + (totalUdioLikes / 50.0)
             + (totalTiktokViews / 10000.0);

        // The final raw score is the weekly growth score, PLUS 1% of the lifetime score.
        // This ensures that weekly growth dictates the chart, but if there's no weekly data (or a tie),
        // the songs with the highest absolute lifetime popularity win out!
        double rawScore = weeklyScore + (lifetimeScore * 0.01);

        // Apply Freshness Multiplier (Billboard style)
        double freshnessMultiplier = 1.0;
        if (track.getReleaseDate() != null && !track.getReleaseDate().isEmpty()) {
            try {
                java.time.LocalDate releaseDate = java.time.LocalDate.parse(track.getReleaseDate());
                long daysSinceRelease = java.time.temporal.ChronoUnit.DAYS.between(
                    releaseDate,
                    java.time.LocalDate.now()
                );

                if (daysSinceRelease <= 14) {
                    freshnessMultiplier = 1.5; // Boost new releases
                } else if (daysSinceRelease > 60) {
                    freshnessMultiplier = 0.5; // Decay older tracks
                }
            } catch (Exception e) {
                // Ignore parse errors, just use default multiplier
            }
        }
        
        return rawScore * freshnessMultiplier;
    }

    private double computeTotalScore(WeeklyChart entry) {
        double localScore = (entry.getUpvoteCount() * UPVOTE_WEIGHT);
        return computeGlobalScore(entry.getTrack()) + localScore;
    }

    private static class TrackScore {
        Track track;
        double score;
        TrackScore(Track track, double score) {
            this.track = track;
            this.score = score;
        }
    }
}
