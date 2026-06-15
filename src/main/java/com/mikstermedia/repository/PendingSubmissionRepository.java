package com.mikstermedia.repository;

import com.mikstermedia.model.PendingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data repository for {@link PendingSubmission} records.
 *
 * <p>All finder methods are used by the admin review panel (future feature)
 * to filter submissions by platform type or creator email.
 */
@Repository
public interface PendingSubmissionRepository extends JpaRepository<PendingSubmission, Long> {

    /** Filter by declared platform for batch review. */
    List<PendingSubmission> findByPlatformType(String platformType);

    /** Look up all submissions from the same email (spam detection). */
    List<PendingSubmission> findBySubmitterEmail(String email);
}
