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
    
    /** Exact match for creator profile pages. */
    List<Track> findByCreatorIgnoreCase(String creator);

    /** Count of distinct streaming platforms in the library (e.g. Spotify, YouTube, Apple). */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(DISTINCT t.platformSource) FROM Track t WHERE t.platformSource IS NOT NULL")
    long countDistinctPlatforms();
}
