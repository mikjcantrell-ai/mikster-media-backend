package com.mikstermedia.repository;

import com.mikstermedia.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data repository for {@link Track} persistence.
 *
 * <p>JpaRepository provides standard CRUD + pagination out of the box.
 * Custom query methods follow Spring Data's derived-query naming conventions.
 */
@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    /** Returns only tracks marked as featured, sorted by admin-set displayOrder. */
    List<Track> findByFeaturedStatusTrueOrderByDisplayOrderAsc();

    /** Finds tracks by streaming platform, e.g. "Spotify", "YouTube", "Apple". */
    List<Track> findByPlatformSource(String platformSource);

    /** Full genre filter for browse-by-genre views. */
    List<Track> findByGenreIgnoreCase(String genre);

    /** Case-insensitive title search for the search bar. */
    List<Track> findByTitleContainingIgnoreCase(String keyword);

    /** Lookup by creator name for artist profile pages. */
    List<Track> findByCreatorContainingIgnoreCase(String creator);

    /** Fetch recent tracks that have a video url. */
    List<Track> findTop25ByVideoUrlIsNotNullAndVideoUrlNotOrderByVideoDisplayOrderAscIdDesc(String emptyString);
    
    /** Exact match for creator profile pages. */
    List<Track> findByCreatorIgnoreCase(String creator);

    /** Count of distinct streaming platforms in the library (e.g. Spotify, YouTube, Apple). */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT t.platformSource) FROM Track t WHERE t.platformSource IS NOT NULL")
    long countDistinctPlatforms();

    /** Temporary migration method to restore upvotes */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(
        value = "UPDATE track t JOIN weekly_chart w ON w.track_id = t.id SET t.upvote_count = w.upvote_count WHERE w.upvote_count > 0 AND (t.upvote_count IS NULL OR t.upvote_count = 0)", 
        nativeQuery = true)
    int restoreUpvotesFromWeeklyChart();
}
