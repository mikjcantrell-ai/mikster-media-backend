package com.mikstermedia.repository;
import com.mikstermedia.model.NewRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface NewReleaseRepository extends JpaRepository<NewRelease, Long> {
    List<NewRelease> findByFeaturedStatusTrueOrderByReleaseDateDesc();
    List<NewRelease> findAllByOrderByReleaseDateDesc();
    List<NewRelease> findByReleaseDateBetweenOrderByReleaseDateDesc(LocalDate startDate, LocalDate endDate);
    List<NewRelease> findByReleaseDateGreaterThanEqualOrderByReleaseDateDesc(LocalDate startDate);
    List<NewRelease> findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate endDate);
    /** Removes all new-release entries for a given track (called before track delete). */
    void deleteByTrackId(Long trackId);
}
