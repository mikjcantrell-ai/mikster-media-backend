package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * JPA entity tracking the Top-10 weekly performance metrics for AI tracks.
 *
 * <p>The {@link #rankChange} field is computed by {@link com.mikstermedia.service.WeeklyChartService}
 * each time rankings are refreshed, comparing the current calculated score
 * against the previously stored rank.
 *
 * <p>A foreign-key constraint links each chart entry to exactly one {@link Track}.
 */
@Entity
@Table(name = "weekly_charts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK reference to the {@code tracks} table.
     * EAGER fetching ensures the full track object is loaded with chart data
     * for the leaderboard response — avoids N+1 in the API response.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    /** Current leaderboard position (1 = top). */
    @Column(name = "current_rank", nullable = false)
    private Integer currentRank;

    /** Position held in the previous week's ranking cycle. */
    @Column(name = "previous_rank")
    private Integer previousRank;

    /** Cumulative upvotes cast by platform users this week. */
    @Column(name = "upvote_count", nullable = false)
    private Integer upvoteCount = 0;

    /** Number of in-platform stream plays recorded this week. */
    @Column(name = "weekly_plays", nullable = false)
    private Integer weeklyPlays = 0;

    /**
     * Human-readable trend indicator computed after each ranking cycle.
     * Allowed values: "UP", "DOWN", "NEW", "STEADY"
     */
    @Column(name = "rank_change", length = 10)
    private String rankChange = "NEW";
}
