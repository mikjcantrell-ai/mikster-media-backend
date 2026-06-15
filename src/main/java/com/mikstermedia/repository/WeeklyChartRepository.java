package com.mikstermedia.repository;

import com.mikstermedia.model.WeeklyChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link WeeklyChart} persistence.
 *
 * <p>The leaderboard query returns results ordered by {@code current_rank}
 * ascending so position 1 appears first in the frontend table.
 */
@Repository
public interface WeeklyChartRepository extends JpaRepository<WeeklyChart, Long> {

    /**
     * Returns all chart entries sorted by rank for the leaderboard view.
     * The EAGER-fetched {@code track} relationship is loaded in this same query.
     */
    List<WeeklyChart> findAllByOrderByCurrentRankAsc();

    /** Retrieves the chart entry for a specific track (used during score refresh). */
    Optional<WeeklyChart> findByTrackId(Long trackId);

    /**
     * Custom JPQL: fetch top-N chart entries.
     * Angular leaderboard requests exactly 10 entries via the service layer.
     */
    @Query("SELECT wc FROM WeeklyChart wc ORDER BY wc.currentRank ASC")
    List<WeeklyChart> findTopChartEntries();

    /** Removes the chart entry for a given track (called before track delete). */
    void deleteByTrackId(Long trackId);
}
