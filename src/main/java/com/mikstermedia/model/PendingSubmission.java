package com.mikstermedia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * JPA entity capturing raw creator submissions awaiting editorial review.
 *
 * <p>Submissions are ingested via the "Submit Your Music" form and remain in
 * this table until a platform editor promotes them to the {@code tracks} table
 * or marks them as rejected.
 *
 * <p>URL validation (domain allow-list for YouTube / Spotify / Apple) is
 * enforced in {@link com.mikstermedia.service.SubmissionService} before any
 * record is persisted here.
 */
@Entity
@Table(name = "pending_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title the creator assigned to their track. */
    @Column(name = "track_title", nullable = false)
    private String trackTitle;

    /** Creator / artist name as self-reported. */
    @Column(name = "artist_name", nullable = false)
    private String artistName;

    /**
     * Validated streaming URL.
     * Must match one of: open.spotify.com, youtube.com, youtu.be, music.apple.com.
     */
    @Column(name = "stream_url", nullable = false, length = 1024)
    private String streamUrl;

    /** Platform selected by the creator: "Spotify", "YouTube", "Apple". */
    @Column(name = "platform_type")
    private String platformType;

    /** Comma-separated list of AI tools the creator declares using. */
    @Column(name = "tools_declared", length = 512)
    private String toolsDeclared;

    /** Contact email for editorial follow-up. */
    @Column(name = "submitter_email", nullable = false)
    private String submitterEmail;

    /** Automatically set to server time when the record is first persisted. */
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    /** Flag indicating priority submission by a Producer tier member. */
    @Column(name = "is_priority", nullable = false)
    private boolean isPriority = false;

    /** Convenience initializer — sets submissionDate to now before insert. */
    @PrePersist
    public void prePersist() {
        if (submissionDate == null) {
            submissionDate = LocalDateTime.now();
        }
    }
}
